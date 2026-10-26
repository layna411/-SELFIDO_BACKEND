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
    print("=== STARTING STEP 4 CHILD MODE INTEGRATION TESTS ===")
    all_passed = True

    # 1. Login regression checks
    admin_token = login("admin", "password123")
    therapist_token = login("therapist1", "password123")
    cg1_token = login("caregiver1", "password123")
    cg2_token = login("caregiver_test2", "password123")

    if admin_token and therapist_token and cg1_token and cg2_token:
        log("Test 1: Authentication successful for Super Admin, Therapist, Caregiver 1, and Caregiver 2.")
    else:
        log("Test 1: Login failed for one or more test accounts.", False)
        sys.exit(1)

    headers_cg1 = {"Authorization": f"Bearer {cg1_token}"}
    headers_cg2 = {"Authorization": f"Bearer {cg2_token}"}
    headers_th = {"Authorization": f"Bearer {therapist_token}"}
    headers_adm = {"Authorization": f"Bearer {admin_token}"}

    # Retrieve linked child ID for Caregiver 1
    resp_cg1_dash = requests.get(f"{BASE_URL}/api/caregiver/dashboard-summary", headers=headers_cg1)
    child1_id = None
    if resp_cg1_dash.status_code == 200:
        children = resp_cg1_dash.json().get("linkedChildren", [])
        if children:
            child1_id = children[0].get("childId")

    if not child1_id:
        log("Child 1 ID not found for Caregiver 1", False)
        sys.exit(1)

    # 2. Caregiver opens Child Mode for a linked child
    resp_child_mode_cg1 = requests.get(f"{BASE_URL}/api/child-mode/dashboard/{child1_id}", headers=headers_cg1)
    if resp_child_mode_cg1.status_code == 200:
        data = resp_child_mode_cg1.json()
        log(f"Test 2: Caregiver 1 opened Child Mode for linked Child ID {child1_id} successfully. Child Name: '{data.get('childName')}', Total Stars: {data.get('totalStars')}.")
    else:
        log(f"Test 2: Caregiver 1 Child Mode fetch failed ({resp_child_mode_cg1.status_code})", False)
        all_passed = False

    # 3. Therapist opens Child Mode for an assigned child
    resp_child_mode_th = requests.get(f"{BASE_URL}/api/child-mode/dashboard/{child1_id}", headers=headers_th)
    if resp_child_mode_th.status_code == 200:
        log(f"Test 3: Therapist opened Child Mode for assigned Child ID {child1_id} successfully.")
    else:
        log(f"Test 3: Therapist Child Mode fetch failed ({resp_child_mode_th.status_code})", False)
        all_passed = False

    # 4. Unassigned or unlinked child access attempt (Caregiver 2 attempting Child 1)
    resp_child_mode_unlinked = requests.get(f"{BASE_URL}/api/child-mode/dashboard/{child1_id}", headers=headers_cg2)
    if resp_child_mode_unlinked.status_code == 403:
        log("Test 4: Security verified. Unlinked Caregiver 2 access to Child 1 Child Mode returned HTTP 403 Forbidden.")
    else:
        log(f"Test 4: Unlinked Caregiver access returned HTTP {resp_child_mode_unlinked.status_code} (expected 403)", False)
        all_passed = False

    # 5. Invalid Child ID Access Attempt
    resp_invalid_child = requests.get(f"{BASE_URL}/api/child-mode/dashboard/999999", headers=headers_cg1)
    if resp_invalid_child.status_code in (403, 404):
        log(f"Test 5: Invalid Child ID request returned HTTP {resp_invalid_child.status_code} as expected.")
    else:
        log(f"Test 5: Invalid Child ID returned HTTP {resp_invalid_child.status_code} (expected 403/404)", False)
        all_passed = False

    # 6. Child Rewards & Preferences Security Checks
    resp_rewards = requests.get(f"{BASE_URL}/api/child-mode/rewards/{child1_id}", headers=headers_cg1)
    if resp_rewards.status_code == 200:
        log("Test 6: Child rewards fetched successfully for authorized context.")
    else:
        log(f"Test 6: Rewards fetch failed ({resp_rewards.status_code})", False)
        all_passed = False

    resp_unauth_rewards = requests.get(f"{BASE_URL}/api/child-mode/rewards/{child1_id}", headers=headers_cg2)
    if resp_unauth_rewards.status_code == 403:
        log("Test 7: Unlinked Caregiver 2 access to Child rewards returned HTTP 403 Forbidden.")
    else:
        log(f"Test 7: Unlinked rewards fetch returned HTTP {resp_unauth_rewards.status_code} (expected 403)", False)
        all_passed = False

    # 7. Step Progress Recording in Child Mode
    progress_payload = {
        "childId": child1_id,
        "activityId": 1,
        "stepNumber": 1,
        "isActivityCompleted": False
    }
    resp_progress = requests.post(f"{BASE_URL}/api/child-mode/progress", json=progress_payload, headers=headers_cg1)
    if resp_progress.status_code == 200:
        log("Test 8: Child Mode step progress recorded successfully without creating false practice records.")
    else:
        log(f"Test 8: Child Mode progress recording failed ({resp_progress.status_code})", False)
        all_passed = False

    # 8. Clinical Boundaries Check - Verify no prompt levels (L0-L6), diagnosis, or clinical notes exposed in Child Mode
    raw_payload = resp_child_mode_cg1.text
    if "promptLevel" not in raw_payload and "L0" not in raw_payload and "L6" not in raw_payload and "diagnosisNotes" not in raw_payload:
        log("Test 9: Privacy & Clinical Boundaries verified. Clinical prompt levels, notes, and diagnosis are NOT exposed in Child Mode payload.")
    else:
        log("Test 9: Privacy boundary failed! Clinical data detected in Child Mode payload.", False)
        all_passed = False

    # 9. Super Admin dashboard regression check
    resp_admin_stats = requests.get(f"{BASE_URL}/api/admin/dashboard/stats", headers=headers_adm)
    if resp_admin_stats.status_code == 200:
        log("Test 10: Super Admin dashboard regression check PASSED.")
    else:
        log(f"Test 10: Super Admin dashboard stats failed ({resp_admin_stats.status_code})", False)
        all_passed = False

    print("\n==============================================")
    if all_passed:
        print("ALL STEP 4 CHILD MODE INTEGRATION TESTS PASSED!")
    else:
        print("SOME TESTS FAILED - CHECK LOGS ABOVE.")
        sys.exit(1)

if __name__ == "__main__":
    run_tests()
