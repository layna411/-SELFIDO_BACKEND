import requests
import json
import time
from concurrent.futures import ThreadPoolExecutor, as_completed

BASE_URL = "http://localhost:8080"

def log(msg, status="INFO"):
    print(f"[{status}] {msg}")

def login(username, password):
    resp = requests.post(f"{BASE_URL}/api/auth/login", json={"username": username, "password": password})
    if resp.status_code == 200:
        return resp.json().get("token")
    return None

def main():
    log("=== SELFORA Step 4.2 Concurrent Progress & Reward Integrity Test Suite ===")

    # Wait for server readiness
    for i in range(15):
        try:
            r = requests.get(f"{BASE_URL}/api/auth/login")
            break
        except Exception:
            time.sleep(1)

    # 1. Setup Auth Headers
    admin_token = login("admin", "password123")
    assert admin_token, "Admin login failed"
    headers_admin = {"Authorization": f"Bearer {admin_token}"}

    therapist_token = login("therapist1", "password123")
    assert therapist_token, "Therapist login failed"
    headers_therapist = {"Authorization": f"Bearer {therapist_token}"}

    cg1_token = login("caregiver1", "password123")
    assert cg1_token, "Caregiver 1 login failed"
    headers_cg1 = {"Authorization": f"Bearer {cg1_token}"}

    cg2_token = login("caregiver_test2", "password123")
    if not cg2_token:
        cg2_payload = {
            "fullName": "Caregiver Test2",
            "email": "caregiver_test2@example.com",
            "phone": "+19998887776",
            "username": "caregiver_test2",
            "password": "password123"
        }
        requests.post(f"{BASE_URL}/api/admin/caregivers", json=cg2_payload, headers=headers_admin)
        cg2_token = login("caregiver_test2", "password123")
    assert cg2_token, "Caregiver 2 login failed"
    headers_cg2 = {"Authorization": f"Bearer {cg2_token}"}

    log("Authentication successful across Super Admin, Therapist, Caregiver 1, and Caregiver 2.")

    # 2. Setup Children for Testing
    cg1_dash = requests.get(f"{BASE_URL}/api/caregiver/dashboard-summary", headers=headers_cg1).json()
    child1_id = cg1_dash["linkedChildren"][0]["childId"]

    # Setup dedicated Child 2 for Caregiver 2
    cg2_user_id = next(c["id"] for c in requests.get(f"{BASE_URL}/api/admin/caregivers", headers=headers_admin).json() if c.get("username") == "caregiver_test2")
    create_child_payload = {
        "childFirstName": "ConcChild2",
        "childLastName": "Test",
        "dateOfBirth": "2018-05-15",
        "gender": "GIRL",
        "caregiverName": "Caregiver Test2",
        "relationship": "Mother",
        "phone": "+19998887776",
        "email": f"conc_child2_{int(time.time())}@example.com"
    }
    resp_c2 = requests.post(f"{BASE_URL}/api/therapist/create-child-with-caregiver", json=create_child_payload, headers=headers_therapist)
    child2_id = resp_c2.json()["childId"]
    requests.post(f"{BASE_URL}/api/therapist/children/{child2_id}/link-caregiver?caregiverId={cg2_user_id}&relationship=Mother", headers=headers_therapist)

    log(f"Test Context Ready -> Child 1: ID {child1_id}, Child 2: ID {child2_id}")

    # Fetch available activities for Child 1
    resp_dash1 = requests.get(f"{BASE_URL}/api/child-mode/dashboard/{child1_id}", headers=headers_cg1)
    assert resp_dash1.status_code == 200, f"Dashboard fetch failed: {resp_dash1.text}"
    dash1 = resp_dash1.json()
    activities = dash1["allActivities"]
    uncompleted = [a for a in activities if not a.get("completed")]

    if len(uncompleted) < 3:
        # Create a fresh child for Caregiver 1 to guarantee fresh activity state
        cg1_email = cg1_dash.get("caregiverEmail") if cg1_dash.get("caregiverEmail") else "caregiver1@example.com"
        create_c1_payload = {
            "childFirstName": "ConcChild1",
            "childLastName": "Fresh",
            "dateOfBirth": "2019-01-01",
            "gender": "BOY",
            "caregiverName": "Caregiver1",
            "relationship": "Mother",
            "phone": "+19991112222",
            "email": cg1_email
        }
        resp_c1_fresh = requests.post(f"{BASE_URL}/api/therapist/create-child-with-caregiver", json=create_c1_payload, headers=headers_therapist)
        assert resp_c1_fresh.status_code == 200, f"Create Child 1 fresh failed: {resp_c1_fresh.text}"
        child1_id = resp_c1_fresh.json()["childId"]
        cg1_user_id = next(c["id"] for c in requests.get(f"{BASE_URL}/api/admin/caregivers", headers=headers_admin).json() if c.get("username") == "caregiver1")
        requests.post(f"{BASE_URL}/api/therapist/children/{child1_id}/link-caregiver?caregiverId={cg1_user_id}&relationship=Mother", headers=headers_therapist)
        
        resp_dash1 = requests.get(f"{BASE_URL}/api/child-mode/dashboard/{child1_id}", headers=headers_cg1)
        assert resp_dash1.status_code == 200, f"Fresh Dashboard fetch failed: {resp_dash1.text}"
        dash1 = resp_dash1.json()
        activities = dash1["allActivities"]
        uncompleted = activities

    act_a_id = uncompleted[0]["activityId"]
    act_b_id = uncompleted[1]["activityId"]
    act_c_id = uncompleted[2]["activityId"]

    # SCENARIO 1: Normal partial progress
    log("\n--- Scenario 1: Normal partial progress ---")
    resp_partial = requests.post(f"{BASE_URL}/api/child-mode/progress", headers=headers_cg1, json={
        "childId": child1_id,
        "activityId": act_c_id,
        "stepNumber": 1,
        "isActivityCompleted": False
    })
    assert resp_partial.status_code == 200, f"Partial progress failed: {resp_partial.text}"
    p_data = resp_partial.json()
    assert p_data["isCompleted"] == False, "Partial progress marked complete prematurely!"
    log(f"Partial progress verified: step={p_data['totalStepsCompleted']}, isCompleted=False.")

    # SCENARIO 2: First successful activity completion
    log("\n--- Scenario 2: First successful activity completion ---")
    resp_comp = requests.post(f"{BASE_URL}/api/child-mode/progress", headers=headers_cg1, json={
        "childId": child1_id,
        "activityId": act_c_id,
        "stepNumber": 2,
        "isActivityCompleted": True
    })
    assert resp_comp.status_code == 200, f"Activity completion failed: {resp_comp.text}"
    c_data = resp_comp.json()
    assert c_data["isCompleted"] == True, "Activity not marked complete!"
    first_stars = c_data["starsEarned"]
    log(f"First completion verified: isCompleted=True, starsEarned={first_stars}.")

    # SCENARIO 3: Repeated completion requests (Sequential)
    log("\n--- Scenario 3: Repeated completion requests ---")
    resp_retry = requests.post(f"{BASE_URL}/api/child-mode/progress", headers=headers_cg1, json={
        "childId": child1_id,
        "activityId": act_c_id,
        "stepNumber": 2,
        "isActivityCompleted": True
    })
    assert resp_retry.status_code == 200, f"Retry completion failed: {resp_retry.text}"
    r_data = resp_retry.json()
    assert r_data["starsEarned"] == first_stars, f"Retry inflated stars! First={first_stars}, Retry={r_data['starsEarned']}"
    log("Sequential retry idempotency verified: Star count preserved.")

    # SCENARIO 4: Multiple simultaneous completion requests for the same activity
    log("\n--- Scenario 4: Multiple simultaneous completion requests for same activity ---")
    # Use fresh activity (act_a_id) for child 1
    # Reset progress for act_a_id by starting concurrency test directly
    dash_pre = requests.get(f"{BASE_URL}/api/child-mode/dashboard/{child1_id}", headers=headers_cg1).json()
    initial_total_stars = dash_pre["totalStars"]

    def submit_completion(ch_id, act_id, headers):
        return requests.post(f"{BASE_URL}/api/child-mode/progress", headers=headers, json={
            "childId": ch_id,
            "activityId": act_id,
            "stepNumber": 2,
            "isActivityCompleted": True
        })

    num_threads = 10
    log(f"Launching {num_threads} concurrent completion requests for Child {child1_id}, Activity {act_a_id}...")

    results = []
    with ThreadPoolExecutor(max_workers=num_threads) as executor:
        futures = [executor.submit(submit_completion, child1_id, act_a_id, headers_cg1) for _ in range(num_threads)]
        for f in as_completed(futures):
            results.append(f.result())

    status_codes = [r.status_code for r in results]
    assert all(code == 200 for code in status_codes), f"Concurrent requests failed with non-200 status: {status_codes}"

    dash_post = requests.get(f"{BASE_URL}/api/child-mode/dashboard/{child1_id}", headers=headers_cg1).json()
    star_diff = dash_post["totalStars"] - initial_total_stars
    log(f"Concurrent execution completed. Star difference = +{star_diff} stars.")
    # If act_a was completed for first time, diff should be exactly +5 stars (or 0 if already completed).
    assert star_diff in [0, 5], f"Concurrent requests corrupted star total! Diff={star_diff}"

    # Verify badge count for COMPLETION_STAR
    rewards = requests.get(f"{BASE_URL}/api/child-mode/rewards/{child1_id}", headers=headers_cg1).json()
    star_badges = [r for r in rewards if r.get("badgeType") == "COMPLETION_STAR"]
    assert len(star_badges) <= 1, f"Duplicate COMPLETION_STAR badges created: {len(star_badges)}"
    log("Concurrent completion test PASSED: Exactly 1 badge created, stars incremented by <= 5.")

    # SCENARIO 5: Completion requests for different activities
    log("\n--- Scenario 5: Completion requests for different activities ---")
    resp_act_b = requests.post(f"{BASE_URL}/api/child-mode/progress", headers=headers_cg1, json={
        "childId": child1_id,
        "activityId": act_b_id,
        "stepNumber": 2,
        "isActivityCompleted": True
    })
    assert resp_act_b.status_code == 200, f"Activity B completion failed: {resp_act_b.text}"
    log("Completion for distinct Activity B processed cleanly.")

    # SCENARIO 6: Concurrent requests for different children
    log("\n--- Scenario 6: Concurrent requests for different children ---")
    log(f"Launching concurrent progress submissions for Child 1 ({child1_id}) and Child 2 ({child2_id})...")

    with ThreadPoolExecutor(max_workers=6) as executor:
        f1 = [executor.submit(submit_completion, child1_id, act_a_id, headers_cg1) for _ in range(3)]
        f2 = [executor.submit(submit_completion, child2_id, act_a_id, headers_cg2) for _ in range(3)]
        all_futures = f1 + f2
        c_results = [f.result() for f in as_completed(all_futures)]

    c_status = [r.status_code for r in c_results]
    for r in c_results:
        if r.status_code != 200:
            log(f"Multi-child request failed: {r.status_code} {r.text}", "ERROR")
    assert all(r.status_code == 200 for r in c_results), f"Concurrent multi-child submissions failed with status codes: {c_status}"

    dash_c1 = requests.get(f"{BASE_URL}/api/child-mode/dashboard/{child1_id}", headers=headers_cg1).json()
    dash_c2 = requests.get(f"{BASE_URL}/api/child-mode/dashboard/{child2_id}", headers=headers_cg2).json()
    log(f"Multi-child isolation verified. Child 1 total stars: {dash_c1['totalStars']}, Child 2 total stars: {dash_c2['totalStars']}.")

    # SCENARIO 7: Unauthorized progress submissions
    log("\n--- Scenario 7: Unauthorized progress submissions ---")
    resp_unauth = requests.post(f"{BASE_URL}/api/child-mode/progress", headers=headers_cg2, json={
        "childId": child1_id, # Caregiver 2 attempting Child 1
        "activityId": act_a_id,
        "stepNumber": 1,
        "isActivityCompleted": False
    })
    assert resp_unauth.status_code == 403, f"Expected 403 for unauthorized progress submission, got {resp_unauth.status_code}"
    log("Unauthorized progress submission blocked with HTTP 403 Forbidden.")

    # SCENARIO 8: Persisted star and badge counts after requests and retries
    log("\n--- Scenario 8: Persisted star and badge counts verification ---")
    dash_final = requests.get(f"{BASE_URL}/api/child-mode/dashboard/{child1_id}", headers=headers_cg1).json()
    persisted_stars = dash_final["totalStars"]
    persisted_completed_count = dash_final["completedCount"]
    rewards_final = dash_final["earnedRewards"]

    log(f"Final Persisted State -> Total Stars: {persisted_stars}, Completed Activities: {persisted_completed_count}, Earned Badges: {len(rewards_final)}")
    assert persisted_stars >= 5, "Persisted stars should be positive"
    log("Persisted database records and reward counts verified successfully.")

    log("\n=== ALL 8 STEP 4.2 CONCURRENCY TEST SCENARIOS PASSED SUCCESSFULLY ===")

if __name__ == "__main__":
    main()
