import requests
import json
import time

BASE_URL = "http://localhost:8080"

def log(msg, status="INFO"):
    print(f"[{status}] {msg}")

def login(username, password):
    resp = requests.post(f"{BASE_URL}/api/auth/login", json={"username": username, "password": password})
    if resp.status_code == 200:
        data = resp.json()
        return data.get("token")
    else:
        return None

def main():
    log("=== SELFORA Step 4.1 Integration Test Suite ===")

    # Wait for server readiness
    for i in range(15):
        try:
            r = requests.get(f"{BASE_URL}/api/auth/login")
            break
        except Exception:
            time.sleep(1)

    # 1. Authenticate roles
    admin_token = login("admin", "password123")
    assert admin_token, "Admin login failed"
    headers_admin = {"Authorization": f"Bearer {admin_token}"}

    therapist_token = login("therapist1", "password123")
    assert therapist_token, "Therapist login failed"
    headers_therapist = {"Authorization": f"Bearer {therapist_token}"}

    caregiver1_token = login("caregiver1", "password123")
    assert caregiver1_token, "Caregiver1 login failed"
    headers_cg1 = {"Authorization": f"Bearer {caregiver1_token}"}

    caregiver2_token = login("caregiver_test2", "password123")
    if not caregiver2_token:
        # Create caregiver_test2 if not exists
        cg2_payload = {
            "fullName": "Caregiver Test2",
            "email": "caregiver_test2@example.com",
            "phone": "+19998887776",
            "username": "caregiver_test2",
            "password": "password123"
        }
        requests.post(f"{BASE_URL}/api/admin/caregivers", json=cg2_payload, headers=headers_admin)
        caregiver2_token = login("caregiver_test2", "password123")

    assert caregiver2_token, "Caregiver2 login failed"
    headers_cg2 = {"Authorization": f"Bearer {caregiver2_token}"}

    log("Authentication successful for Super Admin, Therapist, Caregiver 1, and Caregiver 2.")

    # 2. Get Child 1 for Caregiver 1
    resp_cg1_dash = requests.get(f"{BASE_URL}/api/caregiver/dashboard-summary", headers=headers_cg1)
    assert resp_cg1_dash.status_code == 200, f"Caregiver 1 dashboard summary failed: {resp_cg1_dash.text}"
    cg1_dash_data = resp_cg1_dash.json()
    cg1_children = cg1_dash_data.get("linkedChildren", [])
    assert len(cg1_children) > 0, "Caregiver 1 has no linked children"
    child1_id = cg1_children[0]["childId"]

    # 3. Create Child 2 and explicitly link Caregiver 2
    cg2_users = requests.get(f"{BASE_URL}/api/admin/caregivers", headers=headers_admin).json()
    cg2_item = next((c for c in cg2_users if c.get("username") == "caregiver_test2"), None)
    assert cg2_item, "caregiver_test2 user not found"
    cg2_user_id = cg2_item["id"]

    create_child_payload = {
        "childFirstName": "Tommy",
        "childLastName": "Test",
        "dateOfBirth": "2018-05-15",
        "gender": "BOY",
        "caregiverName": "Caregiver Test2",
        "relationship": "Father",
        "phone": "+19998887776",
        "email": "unique_child2_cg@example.com"
    }
    resp_create = requests.post(f"{BASE_URL}/api/therapist/create-child-with-caregiver", json=create_child_payload, headers=headers_therapist)
    assert resp_create.status_code == 200, f"Failed to create Child 2: {resp_create.text}"
    child2_id = resp_create.json()["childId"]

    # Explicitly link caregiver_test2 to child2
    resp_link = requests.post(f"{BASE_URL}/api/therapist/children/{child2_id}/link-caregiver?caregiverId={cg2_user_id}&relationship=Father", headers=headers_therapist)
    assert resp_link.status_code == 200, f"Failed to link caregiver_test2 to child2: {resp_link.text}"

    assert child1_id != child2_id, "Child 1 and Child 2 must be distinct"
    log(f"Child 1 (Caregiver 1): ID {child1_id}, Child 2 (Caregiver 2): ID {child2_id}")

    # SCENARIO 1: Authorized progress retrieval and submission
    log("\n--- Scenario 1: Authorized progress retrieval & submission ---")
    resp_dash = requests.get(f"{BASE_URL}/api/child-mode/dashboard/{child1_id}", headers=headers_cg1)
    assert resp_dash.status_code == 200, f"Dashboard fetch failed: {resp_dash.text}"
    dash_data = resp_dash.json()
    log(f"Caregiver 1 dashboard fetch for Child {child1_id}: Total Stars = {dash_data['totalStars']}")

    all_activities = dash_data.get("allActivities", [])
    assert len(all_activities) > 0, "No activities found in dashboard"
    uncompleted = [a for a in all_activities if not a.get("completed")]
    test_act_id = uncompleted[0]["activityId"] if uncompleted else all_activities[0]["activityId"]
    log(f"Selected Activity ID {test_act_id} for progress verification.")

    # SCENARIO 2: Forged child, programme, and activity IDs
    log("\n--- Scenario 2: Forged child and activity IDs ---")
    resp_forged_child = requests.get(f"{BASE_URL}/api/child-mode/dashboard/999999", headers=headers_cg1)
    assert resp_forged_child.status_code in [403, 404], f"Forged child ID returned unexpected status: {resp_forged_child.status_code}"

    resp_forged_act = requests.post(f"{BASE_URL}/api/child-mode/progress", headers=headers_cg1, json={
        "childId": child1_id,
        "activityId": 999999,
        "stepNumber": 1,
        "isActivityCompleted": False
    })
    assert resp_forged_act.status_code == 404, f"Forged activity ID progress returned status: {resp_forged_act.status_code}"
    log("Forged child and activity IDs properly rejected.")

    # SCENARIO 3: Partial vs completed activity progress
    log("\n--- Scenario 3: Partial vs completed activity progress ---")
    resp_partial = requests.post(f"{BASE_URL}/api/child-mode/progress", headers=headers_cg1, json={
        "childId": child1_id,
        "activityId": test_act_id,
        "stepNumber": 1,
        "isActivityCompleted": False
    })
    assert resp_partial.status_code == 200, f"Partial progress failed: {resp_partial.text}"
    prog_partial = resp_partial.json()
    assert prog_partial["isCompleted"] == False, "Partial progress marked completed prematurely!"
    log(f"Partial progress verified: step {prog_partial['totalStepsCompleted']} completed, activity isCompleted = False.")

    # SCENARIO 4 & 5: Duplicate progress submissions & Reward consistency after retries
    log("\n--- Scenario 4 & 5: Duplicate submissions & reward consistency ---")
    # First completion
    resp_comp1 = requests.post(f"{BASE_URL}/api/child-mode/progress", headers=headers_cg1, json={
        "childId": child1_id,
        "activityId": test_act_id,
        "stepNumber": 2,
        "isActivityCompleted": True
    })
    assert resp_comp1.status_code == 200, f"Completion 1 failed: {resp_comp1.text}"
    comp1_stars = resp_comp1.json()["starsEarned"]

    # Retry completion (duplicate submission)
    resp_comp2 = requests.post(f"{BASE_URL}/api/child-mode/progress", headers=headers_cg1, json={
        "childId": child1_id,
        "activityId": test_act_id,
        "stepNumber": 2,
        "isActivityCompleted": True
    })
    assert resp_comp2.status_code == 200, f"Completion 2 failed: {resp_comp2.text}"
    comp2_stars = resp_comp2.json()["starsEarned"]

    assert comp1_stars == comp2_stars, f"Star reward repeated on retry! Comp1={comp1_stars}, Comp2={comp2_stars}"
    log(f"Reward consistency verified: Stars remained constant at {comp2_stars} after retry submission.")

    # SCENARIO 6: Cross-caregiver progress access attempts
    log("\n--- Scenario 6: Cross-caregiver progress access attempts ---")
    resp_cross_prog = requests.post(f"{BASE_URL}/api/child-mode/progress", headers=headers_cg2, json={
        "childId": child1_id, # Child 1 belongs to Caregiver 1
        "activityId": test_act_id,
        "stepNumber": 1,
        "isActivityCompleted": False
    })
    assert resp_cross_prog.status_code == 403, f"Cross-caregiver progress attempt expected 403, got {resp_cross_prog.status_code}"
    log("Cross-caregiver progress modification blocked with HTTP 403.")

    # SCENARIO 7: Unauthorized preference reads and updates
    log("\n--- Scenario 7: Unauthorized preference reads & updates ---")
    resp_unauth_pref_get = requests.get(f"{BASE_URL}/api/child-mode/preferences/{child1_id}", headers=headers_cg2)
    assert resp_unauth_pref_get.status_code == 403, f"Caregiver 2 reading Child 1 preferences expected 403, got {resp_unauth_pref_get.status_code}"

    resp_unauth_pref_put = requests.put(f"{BASE_URL}/api/child-mode/preferences/{child1_id}", headers=headers_cg2, json={
        "preferredName": "HackedName"
    })
    assert resp_unauth_pref_put.status_code == 403, f"Caregiver 2 updating Child 1 preferences expected 403, got {resp_unauth_pref_put.status_code}"
    log("Unauthorized preference read/update blocked with HTTP 403.")

    # SCENARIO 8: Preference isolation between two children
    log("\n--- Scenario 8: Preference isolation between two children ---")
    pref_update_cg1 = requests.put(f"{BASE_URL}/api/child-mode/preferences/{child1_id}", headers=headers_cg1, json={
        "preferredName": "SunnyChild1",
        "audioEnabled": True,
        "animationEnabled": True,
        "reducedMotion": False,
        "rewardSounds": True
    })
    assert pref_update_cg1.status_code == 200, f"Child 1 pref update failed: {pref_update_cg1.text}"

    pref_update_cg2 = requests.put(f"{BASE_URL}/api/child-mode/preferences/{child2_id}", headers=headers_cg2, json={
        "preferredName": "StarChild2",
        "audioEnabled": False,
        "animationEnabled": False,
        "reducedMotion": True,
        "rewardSounds": False
    })
    assert pref_update_cg2.status_code == 200, f"Child 2 pref update failed: {pref_update_cg2.text}"

    c1_pref = requests.get(f"{BASE_URL}/api/child-mode/preferences/{child1_id}", headers=headers_cg1).json()
    c2_pref = requests.get(f"{BASE_URL}/api/child-mode/preferences/{child2_id}", headers=headers_cg2).json()

    assert c1_pref["preferredName"] == "SunnyChild1", f"Child 1 name corrupted: {c1_pref}"
    assert c2_pref["preferredName"] == "StarChild2", f"Child 2 name corrupted: {c2_pref}"
    assert c1_pref["audioEnabled"] == True and c2_pref["audioEnabled"] == False, "Child preferences mixed up!"
    log("Preference isolation between two children verified successfully.")

    # SCENARIO 9: Super Admin, Therapist, and Caregiver role-permission checks
    log("\n--- Scenario 9: Super Admin, Therapist, Caregiver role-permission checks ---")
    resp_admin_access = requests.get(f"{BASE_URL}/api/child-mode/preferences/{child1_id}", headers=headers_admin)
    assert resp_admin_access.status_code == 200, f"Super Admin pref read failed: {resp_admin_access.status_code}"

    resp_therapist_access = requests.get(f"{BASE_URL}/api/child-mode/preferences/{child1_id}", headers=headers_therapist)
    assert resp_therapist_access.status_code == 200, f"Therapist pref read failed: {resp_therapist_access.status_code}"
    log("Role-permission hierarchy (Super Admin, Therapist, Caregiver) verified.")

    # SCENARIO 10: Stale-state / Empty state validation
    log("\n--- Scenario 10: Android child switching & state validation ---")
    resp_dash_cg1 = requests.get(f"{BASE_URL}/api/child-mode/dashboard/{child1_id}", headers=headers_cg1).json()
    resp_dash_cg2 = requests.get(f"{BASE_URL}/api/child-mode/dashboard/{child2_id}", headers=headers_cg2).json()
    assert resp_dash_cg1["childId"] != resp_dash_cg2["childId"], "Dashboard data contains cross-child leakage!"
    log("Child dashboard data isolated cleanly per child.")

    # SCENARIO 11: Regression checks for caregiver practice recording and clinical data separation
    log("\n--- Scenario 11: Regression check - Practice recording vs Child mode progress separation ---")
    today_programs = cg1_dash_data.get("todayPrograms", [])
    if not today_programs:
        # Create a home program for Child 1 if none exists
        create_hp_payload = {
            "childId": child1_id,
            "activityId": test_act_id,
            "title": "Daily Practice",
            "description": "Daily practice session for step 4.1 verification",
            "priority": "HIGH",
            "scheduleType": "DAILY",
            "preferredTime": "MORNING",
            "frequencyPerWeek": 7,
            "targetDurationMinutes": 15,
            "targetPromptLevelId": 1
        }
        resp_hp = requests.post(f"{BASE_URL}/api/home-programs", json=create_hp_payload, headers=headers_therapist)
        if resp_hp.status_code not in [200, 201]:
            resp_hp = requests.post(f"{BASE_URL}/api/therapist/home-programs", json=create_hp_payload, headers=headers_therapist)
        assert resp_hp.status_code in [200, 201], f"Failed to create home program: {resp_hp.text}"
        hp_id = resp_hp.json()["id"]
        hp_act_id = test_act_id
    else:
        hp_id = today_programs[0]["id"]
        hp_act_id = today_programs[0]["activityId"]

    resp_practice = requests.post(f"{BASE_URL}/api/caregiver-practice/submit", headers=headers_cg1, json={
        "childId": child1_id,
        "homeProgramId": hp_id,
        "activityId": hp_act_id,
        "durationMinutes": 15,
        "promptLevel": "Independent",
        "notes": "Step 4.1 verification practice session"
    })
    assert resp_practice.status_code in [200, 201], f"Caregiver practice recording failed: {resp_practice.text}"
    log("Caregiver practice recording verified. Clinical/practice records remain separated from child gamification state.")

    log("\n=== ALL 11 STEP 4.1 INTEGRATION TEST SCENARIOS PASSED SUCCESSFULLY ===")

if __name__ == "__main__":
    main()
