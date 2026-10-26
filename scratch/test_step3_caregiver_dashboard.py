import requests
import json
import sys

BASE_URL = "http://localhost:8080"

def log(msg, success=True):
    prefix = "[PASS]" if success else "[FAIL]"
    print(f"{prefix} {msg}")

def test_login(username, password):
    url = f"{BASE_URL}/api/auth/login"
    payload = {"username": username, "password": password}
    resp = requests.post(url, json=payload, headers={"Content-Type": "application/json"})
    return resp

def run_tests():
    print("=== STARTING STEP 3 CAREGIVER INTEGRATION TESTS ===")
    all_passed = True

    # Test 1: Unauthenticated request to caregiver dashboard
    resp = requests.get(f"{BASE_URL}/api/caregiver/dashboard-summary")
    if resp.status_code == 401:
        log("Test 1: Unauthenticated caregiver dashboard request returned HTTP 401 as expected.")
    else:
        log(f"Test 1: Expected 401, got {resp.status_code}", False)
        all_passed = False

    # Test 2: Invalid password
    resp = test_login("caregiver1", "wrongpassword")
    if resp.status_code == 401:
        log("Test 2: Invalid caregiver login returned HTTP 401.")
    else:
        log(f"Test 2: Expected 401 for invalid password, got {resp.status_code}", False)
        all_passed = False

    # Test 3: Valid Super Admin login regression
    resp = test_login("admin", "password123")
    if resp.status_code == 200:
        data = resp.json()
        roles = data.get("roles", [])
        if "ROLE_SUPER_ADMIN" in roles or "SUPER_ADMIN" in roles:
            log("Test 3: Super Admin login succeeded and returned admin role.")
        else:
            log(f"Test 3: Super Admin login missing admin role: {roles}", False)
            all_passed = False
    else:
        log(f"Test 3: Super Admin login failed with status {resp.status_code}", False)
        all_passed = False

    # Test 4: Valid Therapist login regression
    resp = test_login("therapist1", "password123")
    if resp.status_code == 200:
        data = resp.json()
        roles = data.get("roles", [])
        if "ROLE_THERAPIST" in roles or "THERAPIST" in roles:
            log("Test 4: Therapist login succeeded and returned therapist role.")
        else:
            log(f"Test 4: Therapist login missing therapist role: {roles}", False)
            all_passed = False
    else:
        log(f"Test 4: Therapist login failed with status {resp.status_code}", False)
        all_passed = False

    # Test 5: Valid Caregiver login
    resp = test_login("caregiver1", "password123")
    caregiver_token = None
    if resp.status_code == 200:
        data = resp.json()
        roles = data.get("roles", [])
        caregiver_token = data.get("token")
        must_change_pw = data.get("mustChangePassword", False)
        if ("ROLE_CAREGIVER" in roles or "CAREGIVER" in roles) and caregiver_token:
            log(f"Test 5: Caregiver login succeeded, returned token and ROLE_CAREGIVER (mustChangePassword={must_change_pw}).")
        else:
            log(f"Test 5: Caregiver login response invalid: roles={roles}, token_present={bool(caregiver_token)}", False)
            all_passed = False
    else:
        log(f"Test 5: Caregiver login failed with status {resp.status_code}", False)
        all_passed = False

    if not caregiver_token:
        print("Aborting remaining tests because caregiver login failed.")
        sys.exit(1)

    headers = {"Authorization": f"Bearer {caregiver_token}"}

    # Test 6: Caregiver Dashboard Summary
    resp = requests.get(f"{BASE_URL}/api/caregiver/dashboard-summary", headers=headers)
    if resp.status_code == 200:
        dash_data = resp.json()
        linked_children = dash_data.get("linkedChildren", [])
        total_children = dash_data.get("totalChildrenCount", 0)
        active_progs = dash_data.get("activeProgrammesCount", 0)
        recent_sessions = dash_data.get("recentPracticeSessions", [])
        log(f"Test 6: Caregiver dashboard summary fetched successfully. Total Linked Children={total_children}, Active Programmes={active_progs}, Recent Sessions={len(recent_sessions)}.")
    else:
        log(f"Test 6: Caregiver dashboard summary failed with status {resp.status_code}", False)
        all_passed = False

    # Test 7: Role Isolation - Caregiver accessing Super Admin endpoint
    resp = requests.get(f"{BASE_URL}/api/admin/caregivers", headers=headers)
    if resp.status_code == 403:
        log("Test 7: Role isolation verified. Caregiver request to Super Admin endpoint /api/admin/caregivers returned HTTP 403 Forbidden.")
    else:
        log(f"Test 7: Expected HTTP 403 for Super Admin endpoint access by Caregiver, got {resp.status_code}", False)
        all_passed = False

    # Test 8: Data Isolation - Caregiver attempting to access child not linked to them or child details endpoint validation
    # Let's test GET /api/children/999999 or child endpoints
    resp = requests.get(f"{BASE_URL}/api/children/999999", headers=headers)
    if resp.status_code in (403, 404):
        log(f"Test 8: Data isolation verified. Caregiver request for unlinked/invalid child returned HTTP {resp.status_code}.")
    else:
        log(f"Test 8: Caregiver request for unlinked child returned HTTP {resp.status_code} (expected 403 or 404)", False)
        all_passed = False

    # Test 9: Clinical prompt boundary - Verify dashboard payload has no prompt levels L0-L6 or clinical notes exposed
    resp = requests.get(f"{BASE_URL}/api/caregiver/dashboard-summary", headers=headers)
    body_str = resp.text
    if "promptLevel" not in body_str and "L0" not in body_str and "L6" not in body_str:
        log("Test 9: Clinical boundary verified. Dashboard response does not expose clinical prompt levels L0-L6.")
    else:
        log("Test 9: Clinical prompt levels detected in caregiver response payload!", False)
        all_passed = False

    print("\n==============================================")
    if all_passed:
        print("ALL STEP 3 CAREGIVER INTEGRATION TESTS PASSED!")
    else:
        print("SOME TESTS FAILED - CHECK LOGS ABOVE.")
        sys.exit(1)

if __name__ == "__main__":
    run_tests()
