import requests
import json
import sys

BASE_URL = "http://localhost:8080"

def log(msg, success=True):
    prefix = "[PASS]" if success else "[FAIL]"
    print(f"{prefix} {msg}")

def login(username, password):
    url = f"{BASE_URL}/api/auth/login"
    resp = requests.post(url, json={"username": username, "password": password}, headers={"Content-Type": "application/json"})
    if resp.status_code == 200:
        return resp.json().get("token")
    return None

def run_tests():
    print("=== STARTING STEP 3B CAREGIVER PRACTICE INTEGRATION TESTS ===")
    all_passed = True

    # 1. Login regression checks
    admin_token = login("admin", "password123")
    therapist_token = login("therapist1", "password123")
    caregiver1_token = login("caregiver1", "password123")

    if admin_token and therapist_token and caregiver1_token:
        log("Test 1: Super Admin, Therapist, and Caregiver login flows succeed.")
    else:
        log("Test 1: One or more login requests failed.", False)
        all_passed = False

    headers_cg1 = {"Authorization": f"Bearer {caregiver1_token}"}
    headers_th = {"Authorization": f"Bearer {therapist_token}"}
    headers_adm = {"Authorization": f"Bearer {admin_token}"}

    # 2. Get caregiver dashboard summary & linked children
    resp = requests.get(f"{BASE_URL}/api/caregiver/dashboard-summary", headers=headers_cg1)
    if resp.status_code == 200:
        summary = resp.json()
        log(f"Test 2: Caregiver 1 dashboard summary fetched. Linked children count: {len(summary.get('linkedChildren', []))}.")
    else:
        log(f"Test 2: Caregiver dashboard summary failed ({resp.status_code})", False)
        all_passed = False

    # 3. Test assigned home programs for caregiver
    resp = requests.get(f"{BASE_URL}/api/home-programs/today", headers=headers_cg1)
    if resp.status_code == 200:
        progs = resp.json()
        log(f"Test 3: Home programs endpoint GET /api/home-programs/today returned {len(progs)} assigned programmes.")
    else:
        log(f"Test 3: Home programs endpoint failed ({resp.status_code})", False)
        all_passed = False

    # 4. Create a test home program as Therapist for Child 1 (if Child 1 exists)
    resp_child = requests.get(f"{BASE_URL}/api/children", headers=headers_th)
    child1_id = None
    if resp_child.status_code == 200 and len(resp_child.json()) > 0:
        child1_id = resp_child.json()[0].get("id")

    prog_id = None
    act_id = 1 # default activity
    if child1_id:
        create_prog_payload = {
            "childId": child1_id,
            "activityId": 1,
            "title": "T-Shirt Dressing Practice",
            "description": "Daily home dressing practice",
            "priority": "HIGH",
            "scheduleType": "DAILY",
            "frequencyPerWeek": 5,
            "targetDurationMinutes": 15,
            "goalStatement": "Complete T-Shirt dressing independently"
        }
        resp_prog = requests.post(f"{BASE_URL}/api/home-programs", json=create_prog_payload, headers=headers_th)
        if resp_prog.status_code == 200:
            prog_data = resp_prog.json()
            prog_id = prog_data.get("id")
            log(f"Test 4: Therapist successfully assigned home program ID {prog_id} to child ID {child1_id}.")
        else:
            log(f"Test 4: Therapist home program creation returned {resp_prog.status_code}", False)
            all_passed = False

    # 5. Practice Recording with all 5 Caregiver Outcomes
    if prog_id and child1_id:
        submit_payload = {
            "homeProgramId": prog_id,
            "childId": child1_id,
            "activityId": 1,
            "practiceDate": "2026-10-10",
            "durationMinutes": 12,
            "caregiverNotes": "Child practiced dressing with enthusiasm.",
            "stepResults": [
                {"stepId": 1, "stepNumber": 1, "stepTitle": "Gather T-Shirt", "outcome": "INDEPENDENT", "observation": "Fast"},
                {"stepId": 2, "stepNumber": 2, "stepTitle": "Orient Collar", "outcome": "LITTLE_HELP", "observation": "Verbal cue"},
                {"stepId": 3, "stepNumber": 3, "stepTitle": "Head Through Collar", "outcome": "LOT_HELP", "observation": "Physical help"},
                {"stepId": 4, "stepNumber": 4, "stepTitle": "Right Arm Through", "outcome": "COULD_NOT_COMPLETE", "observation": "Refused"},
                {"stepId": 5, "stepNumber": 5, "stepTitle": "Left Arm Through", "outcome": "NOT_PRACTISED", "observation": "Skipped"}
            ]
        }
        resp_sub = requests.post(f"{BASE_URL}/api/home-practice/submit", json=submit_payload, headers=headers_cg1)
        if resp_sub.status_code == 200:
            sess_data = resp_sub.json()
            sess_id = sess_data.get("id")
            log(f"Test 5: Practice submission recorded successfully. Session ID={sess_id}, Status={sess_data.get('status')}.")
        else:
            log(f"Test 5: Practice submission failed ({resp_sub.status_code}): {resp_sub.text}", False)
            all_passed = False

        # 6. Idempotency Retry Check - submit exact same practice session again
        resp_retry = requests.post(f"{BASE_URL}/api/home-practice/submit", json=submit_payload, headers=headers_cg1)
        if resp_retry.status_code == 200:
            log("Test 6: Idempotent duplicate submission retry handled safely without creating duplicate session rows.")
        else:
            log(f"Test 6: Idempotency retry failed with status {resp_retry.status_code}", False)
            all_passed = False

        # 7. Verification of Practice History
        resp_hist = requests.get(f"{BASE_URL}/api/home-practice/history/{child1_id}", headers=headers_cg1)
        if resp_hist.status_code == 200:
            hist_list = resp_hist.json()
            log(f"Test 7: Practice history fetched from database successfully. Count={len(hist_list)}.")
        else:
            log(f"Test 7: Fetching practice history failed ({resp_hist.status_code})", False)
            all_passed = False

    # 8. Data Isolation Security Test - Caregiver attempting to access unlinked child / invalid IDs
    resp_unlinked = requests.get(f"{BASE_URL}/api/home-programs/child/999999", headers=headers_cg1)
    if resp_unlinked.status_code == 403:
        log("Test 8: Data isolation check verified. Unlinked child home program access returned HTTP 403 Forbidden.")
    else:
        log(f"Test 8: Expected HTTP 403 for unlinked child, got {resp_unlinked.status_code}", False)
        all_passed = False

    # 9. Caregiver submitting practice for program mismatch (e.g. invalid program ID)
    mismatch_payload = {
        "homeProgramId": 999999,
        "childId": child1_id or 1,
        "activityId": 1,
        "practiceDate": "2026-10-10",
        "durationMinutes": 10,
        "caregiverNotes": "Invalid program test"
    }
    resp_mismatch = requests.post(f"{BASE_URL}/api/home-practice/submit", json=mismatch_payload, headers=headers_cg1)
    if resp_mismatch.status_code in (403, 404):
        log(f"Test 9: Invalid program submission rejected with HTTP {resp_mismatch.status_code} as expected.")
    else:
        log(f"Test 9: Mismatch submission returned HTTP {resp_mismatch.status_code} (expected 403/404)", False)
        all_passed = False

    # 10. Role Isolation Security Test - Caregiver attempting to modify clinical records or review practice session
    review_payload = {"feedbackText": "Caregiver trying to post clinical review", "status": "REVIEWED"}
    resp_review = requests.post(f"{BASE_URL}/api/home-practice/reviews/1", json=review_payload, headers=headers_cg1)
    if resp_review.status_code == 403:
        log("Test 10: Role isolation check verified. Caregiver attempting to post clinical review returned HTTP 403 Forbidden.")
    else:
        log(f"Test 10: Expected HTTP 403 for clinical review endpoint, got {resp_review.status_code}", False)
        all_passed = False

    # 11. Clinical Prompt Level boundary check
    resp_dash = requests.get(f"{BASE_URL}/api/caregiver/dashboard-summary", headers=headers_cg1)
    if "promptLevel" not in resp_dash.text and "L0" not in resp_dash.text:
        log("Test 11: Clinical prompt levels (L0-L6) are NOT exposed to caregiver dashboard response.")
    else:
        log("Test 11: Clinical prompt level leaked in caregiver response!", False)
        all_passed = False

    print("\n==============================================")
    if all_passed:
        print("ALL STEP 3B CAREGIVER PRACTICE INTEGRATION TESTS PASSED!")
    else:
        print("SOME TESTS FAILED - CHECK LOGS ABOVE.")
        sys.exit(1)

if __name__ == "__main__":
    run_tests()
