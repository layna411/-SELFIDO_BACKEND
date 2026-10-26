import requests
import json
import sys
import uuid

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
    print("=== STARTING STEP 3B.1 CAREGIVER PRACTICE HARDENING TESTS ===")
    all_passed = True

    # 1. Login check
    admin_token = login("admin", "password123")
    therapist_token = login("therapist1", "password123")
    cg1_token = login("caregiver1", "password123")

    if admin_token and therapist_token and cg1_token:
        log("Test 1: Authentication successful for Super Admin, Therapist, and Caregiver 1.")
    else:
        log("Test 1: Authentication failed for one or more roles.", False)
        sys.exit(1)

    headers_cg1 = {"Authorization": f"Bearer {cg1_token}"}
    headers_th = {"Authorization": f"Bearer {therapist_token}"}
    headers_adm = {"Authorization": f"Bearer {admin_token}"}

    # Create Caregiver 2 for cross-caregiver isolation testing
    cg2_payload = {
        "username": "caregiver_test2",
        "email": "cg2_test@selfora.org",
        "password": "password123",
        "fullName": "Caregiver Two Test",
        "phone": "+1999888777"
    }
    resp_create_cg2 = requests.post(f"{BASE_URL}/api/admin/caregivers", json=cg2_payload, headers=headers_adm)
    cg2_token = login("caregiver_test2", "password123")
    if not cg2_token and resp_create_cg2.status_code == 409:
        cg2_token = login("caregiver_test2", "password123")

    if cg2_token:
        log("Test 2: Caregiver 2 created/authenticated for data-isolation testing.")
    else:
        log(f"Test 2: Failed to log in as Caregiver 2 ({resp_create_cg2.status_code})", False)
        all_passed = False

    headers_cg2 = {"Authorization": f"Bearer {cg2_token}"}

    # 3. Verify Programme Assignment & Initial State Explanation
    resp_init_progs = requests.get(f"{BASE_URL}/api/home-programs/today", headers=headers_cg1)
    if resp_init_progs.status_code == 200:
        count = len(resp_init_progs.json())
        log(f"Test 3: Initial programme fetch returned {count} programmes for Caregiver 1. (Expected cause: no home_programs record seeded for child until therapist assignment).")
    else:
        log(f"Test 3: Initial programme fetch failed ({resp_init_progs.status_code})", False)
        all_passed = False

    # Get child ID for Caregiver 1
    resp_summary = requests.get(f"{BASE_URL}/api/caregiver/dashboard-summary", headers=headers_cg1)
    child1_id = None
    if resp_summary.status_code == 200:
        children = resp_summary.json().get("linkedChildren", [])
        if children:
            child1_id = children[0].get("childId")

    if not child1_id:
        log("Could not find linked child for Caregiver 1.", False)
        sys.exit(1)

    # Therapist assigns home programme for Child 1
    prog_payload = {
        "childId": child1_id,
        "activityId": 1,
        "title": "Evening Dressing Practice",
        "description": "Practice T-shirt dressing at bedtime",
        "priority": "HIGH",
        "scheduleType": "DAILY",
        "frequencyPerWeek": 7,
        "targetDurationMinutes": 15,
        "goalStatement": "Independent dressing within 10 minutes"
    }
    resp_prog = requests.post(f"{BASE_URL}/api/home-programs", json=prog_payload, headers=headers_th)
    prog1_id = None
    if resp_prog.status_code == 200:
        prog1_id = resp_prog.json().get("id")
        log(f"Test 4: Therapist assigned Home Programme ID {prog1_id} to Child ID {child1_id}.")
    else:
        log(f"Test 4: Home programme creation failed ({resp_prog.status_code})", False)
        all_passed = False

    # Caregiver 1 retrieves assigned programme
    resp_progs_now = requests.get(f"{BASE_URL}/api/home-programs/today", headers=headers_cg1)
    if resp_progs_now.status_code == 200 and len(resp_progs_now.json()) > 0:
        log(f"Test 5: Caregiver 1 retrieved active programme list after assignment. Count={len(resp_progs_now.json())}.")
    else:
        log(f"Test 5: Caregiver 1 failed to retrieve programme list after assignment ({resp_progs_now.status_code})", False)
        all_passed = False

    # 4. Idempotency & Session History Tests
    client_sess_id1 = f"SESSION-UUID-{uuid.uuid4()}"
    submit_payload1 = {
        "clientSessionId": client_sess_id1,
        "homeProgramId": prog1_id,
        "childId": child1_id,
        "activityId": 1,
        "practiceDate": "2026-10-10",
        "durationMinutes": 15,
        "caregiverNotes": "First session of the day - great focus!",
        "stepResults": [
            {"stepId": 1, "stepNumber": 1, "stepTitle": "Gather T-Shirt", "outcome": "INDEPENDENT"},
            {"stepId": 2, "stepNumber": 2, "stepTitle": "Orient Collar", "outcome": "LITTLE_HELP"}
        ]
    }

    resp_sess1 = requests.post(f"{BASE_URL}/api/home-practice/submit", json=submit_payload1, headers=headers_cg1)
    sess1_id = None
    if resp_sess1.status_code == 200:
        sess1_id = resp_sess1.json().get("id")
        log(f"Test 6: Practice session 1 recorded successfully. Database ID={sess1_id}, clientSessionId={client_sess_id1}.")
    else:
        log(f"Test 6: Practice session 1 recording failed ({resp_sess1.status_code}): {resp_sess1.text}", False)
        all_passed = False

    # 5. Idempotency Retry Test: Retry exact same payload with client_sess_id1
    resp_retry = requests.post(f"{BASE_URL}/api/home-practice/submit", json=submit_payload1, headers=headers_cg1)
    if resp_retry.status_code == 200:
        retried_id = resp_retry.json().get("id")
        if retried_id == sess1_id:
            log(f"Test 7: Idempotency verified. Retrying clientSessionId '{client_sess_id1}' returned existing database Session ID {sess1_id} without duplicate rows.")
        else:
            log(f"Test 7: Idempotency failed! Retried request created new session ID {retried_id} instead of returning {sess1_id}.", False)
            all_passed = False
    else:
        log(f"Test 7: Idempotency retry request failed ({resp_retry.status_code})", False)
        all_passed = False

    # 6. Genuinely New Session on Same Day Test
    client_sess_id2 = f"SESSION-UUID-{uuid.uuid4()}"
    submit_payload2 = {
        "clientSessionId": client_sess_id2,
        "homeProgramId": prog1_id,
        "childId": child1_id,
        "activityId": 1,
        "practiceDate": "2026-10-10",
        "durationMinutes": 10,
        "caregiverNotes": "Second session later in evening - minor fatigue",
        "stepResults": [
            {"stepId": 1, "stepNumber": 1, "stepTitle": "Gather T-Shirt", "outcome": "INDEPENDENT"},
            {"stepId": 2, "stepNumber": 2, "stepTitle": "Orient Collar", "outcome": "LOT_HELP"}
        ]
    }

    resp_sess2 = requests.post(f"{BASE_URL}/api/home-practice/submit", json=submit_payload2, headers=headers_cg1)
    sess2_id = None
    if resp_sess2.status_code == 200:
        sess2_id = resp_sess2.json().get("id")
        if sess2_id != sess1_id:
            log(f"Test 8: Genuinely new session on same day recorded distinct database Session ID {sess2_id}.")
        else:
            log("Test 8: Genuinely new session improperly overwritten existing session!", False)
            all_passed = False
    else:
        log(f"Test 8: Second session submission failed ({resp_sess2.status_code})", False)
        all_passed = False

    # 7. Check Practice History
    resp_hist = requests.get(f"{BASE_URL}/api/home-practice/history/{child1_id}", headers=headers_cg1)
    if resp_hist.status_code == 200:
        history = resp_hist.json()
        ids = [h.get("id") for h in history]
        if sess1_id in ids and sess2_id in ids:
            log(f"Test 9: Practice history verified. Contains both Session ID {sess1_id} and Session ID {sess2_id}.")
        else:
            log(f"Test 9: Practice history missing sessions: found {ids}", False)
            all_passed = False
    else:
        log(f"Test 9: Fetching practice history failed ({resp_hist.status_code})", False)
        all_passed = False

    # 8. Cross-Caregiver Data Isolation Tests
    # Caregiver 2 attempts to read Caregiver 1's child home programs
    resp_cg2_read = requests.get(f"{BASE_URL}/api/home-programs/child/{child1_id}", headers=headers_cg2)
    if resp_cg2_read.status_code == 403:
        log("Test 10: Caregiver 2 attempt to read Caregiver 1's child programme returned HTTP 403 Forbidden.")
    else:
        log(f"Test 10: Caregiver 2 read attempt returned HTTP {resp_cg2_read.status_code} (expected 403)", False)
        all_passed = False

    # Caregiver 2 attempts to record practice for Caregiver 1's child/programme
    unauth_submit_payload = {
        "clientSessionId": f"SESSION-UNAUTH-{uuid.uuid4()}",
        "homeProgramId": prog1_id,
        "childId": child1_id,
        "activityId": 1,
        "practiceDate": "2026-10-10",
        "durationMinutes": 10,
        "caregiverNotes": "Unauthorized attempt"
    }
    resp_cg2_submit = requests.post(f"{BASE_URL}/api/home-practice/submit", json=unauth_submit_payload, headers=headers_cg2)
    if resp_cg2_submit.status_code == 403:
        log("Test 11: Caregiver 2 attempt to record practice for Caregiver 1's child returned HTTP 403 Forbidden.")
    else:
        log(f"Test 11: Caregiver 2 practice submit attempt returned HTTP {resp_cg2_submit.status_code} (expected 403)", False)
        all_passed = False

    # Caregiver 1 attempts therapist-only review endpoint
    resp_review_unauth = requests.post(f"{BASE_URL}/api/home-practice/reviews/{sess1_id}", json={"status": "REVIEWED"}, headers=headers_cg1)
    if resp_review_unauth.status_code == 403:
        log("Test 12: Caregiver 1 attempt to access therapist-only review endpoint returned HTTP 403 Forbidden.")
    else:
        log(f"Test 12: Caregiver 1 review attempt returned HTTP {resp_review_unauth.status_code} (expected 403)", False)
        all_passed = False

    # Caregiver 1 attempts Super Admin endpoint
    resp_admin_unauth = requests.get(f"{BASE_URL}/api/admin/caregivers", headers=headers_cg1)
    if resp_admin_unauth.status_code == 403:
        log("Test 13: Caregiver 1 attempt to access Super Admin endpoint returned HTTP 403 Forbidden.")
    else:
        log(f"Test 13: Caregiver 1 admin attempt returned HTTP {resp_admin_unauth.status_code} (expected 403)", False)
        all_passed = False

    # 9. Clinical Prompt Level Boundary Check
    resp_dash = requests.get(f"{BASE_URL}/api/caregiver/dashboard-summary", headers=headers_cg1)
    if "promptLevel" not in resp_dash.text and "L0" not in resp_dash.text:
        log("Test 14: Clinical prompt levels (L0-L6) are NOT exposed in caregiver dashboard response.")
    else:
        log("Test 14: Clinical prompt levels leaked in caregiver response!", False)
        all_passed = False

    print("\n==============================================")
    if all_passed:
        print("ALL STEP 3B.1 HARDENING INTEGRATION TESTS PASSED!")
    else:
        print("SOME TESTS FAILED - CHECK LOGS ABOVE.")
        sys.exit(1)

if __name__ == "__main__":
    run_tests()
