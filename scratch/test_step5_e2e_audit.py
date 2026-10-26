import requests
import json
import time
import uuid
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
    log("=== SELFORA Step 5: Full-Application E2E Integration and Release Readiness Audit ===")

    # ---------------------------------------------------------
    # 1. AUTHENTICATION & TOKENS
    # ---------------------------------------------------------
    log("\n--- SECTION 1: AUTHENTICATION & ROLE TOKENS ---")
    admin_token = login("admin", "password123")
    assert admin_token, "Super Admin login failed"
    headers_admin = {"Authorization": f"Bearer {admin_token}"}

    therapist_token = login("therapist1", "password123")
    assert therapist_token, "Therapist login failed"
    headers_therapist = {"Authorization": f"Bearer {therapist_token}"}

    cg1_token = login("caregiver1", "password123")
    assert cg1_token, "Caregiver 1 login failed"
    headers_cg1 = {"Authorization": f"Bearer {cg1_token}"}

    log("Tokens retrieved successfully for Super Admin, Therapist, and Caregiver 1.")

    # ---------------------------------------------------------
    # 2. END-TO-END ROLE WORKFLOWS
    # ---------------------------------------------------------
    log("\n--- SECTION 2: END-TO-END ROLE WORKFLOWS ---")

    # 2.1 SUPER ADMIN WORKFLOW
    log("\n[2.1] Super Admin Workflow:")
    r_admin_dash = requests.get(f"{BASE_URL}/api/admin/dashboard/stats", headers=headers_admin)
    assert r_admin_dash.status_code == 200, f"Admin dash failed: {r_admin_dash.text}"
    log(f"Admin Dashboard Metrics: {r_admin_dash.json()}")

    # Create new test Therapist
    th_name = f"e2e_therapist_{int(time.time())}"
    r_create_th = requests.post(f"{BASE_URL}/api/admin/therapists", headers=headers_admin, json={
        "fullName": "E2E Therapist",
        "email": f"{th_name}@example.com",
        "phone": "+18005550199",
        "username": th_name,
        "password": "password123",
        "licenseNumber": f"LIC-{int(time.time())}"
    })
    assert r_create_th.status_code == 200, f"Create therapist failed: {r_create_th.text}"
    new_th_id = r_create_th.json()["id"]
    log(f"Created Therapist via Super Admin: ID {new_th_id}")

    # Create new test Caregiver
    cg_name = f"e2e_caregiver_{int(time.time())}"
    r_create_cg = requests.post(f"{BASE_URL}/api/admin/caregivers", headers=headers_admin, json={
        "fullName": "E2E Caregiver",
        "email": f"{cg_name}@example.com",
        "phone": "+18005550299",
        "username": cg_name,
        "password": "password123"
    })
    assert r_create_cg.status_code == 200, f"Create caregiver failed: {r_create_cg.text}"
    new_cg_id = r_create_cg.json()["id"]
    log(f"Created Caregiver via Super Admin: ID {new_cg_id}")

    # Fetch Audit Logs
    r_audit = requests.get(f"{BASE_URL}/api/admin/audit-logs", headers=headers_admin)
    assert r_audit.status_code == 200, f"Fetch audit logs failed: {r_audit.text}"
    log(f"Super Admin Audit Logs retrieved: {len(r_audit.json())} entries.")

    # 2.2 THERAPIST WORKFLOW
    log("\n[2.2] Therapist Workflow:")
    r_th_dash = requests.get(f"{BASE_URL}/api/therapist/dashboard-summary", headers=headers_therapist)
    assert r_th_dash.status_code == 200, f"Therapist dash failed: {r_th_dash.text}"
    th_dash = r_th_dash.json()
    log(f"Therapist Dashboard Summary retrieved: {th_dash.get('therapistName')}, active children={len(th_dash.get('activeChildren', []))}")

    # Create child for Therapist & Link Caregiver
    r_create_ch = requests.post(f"{BASE_URL}/api/therapist/create-child-with-caregiver", headers=headers_therapist, json={
        "childFirstName": "E2EChild",
        "childLastName": "Integrity",
        "dateOfBirth": "2019-03-20",
        "gender": "BOY",
        "caregiverName": "E2E Caregiver",
        "relationship": "Father",
        "phone": "+18005550299",
        "email": f"{cg_name}@example.com"
    })
    assert r_create_ch.status_code == 200, f"Therapist child creation failed: {r_create_ch.text}"
    e2e_child_id = r_create_ch.json()["childId"]
    log(f"Therapist created child E2EChild: ID {e2e_child_id}")

    # Link Caregiver 1 to e2e_child_id for caregiver testing
    cg1_user_id = next(c["id"] for c in requests.get(f"{BASE_URL}/api/admin/caregivers", headers=headers_admin).json() if c.get("username") == "caregiver1")
    requests.post(f"{BASE_URL}/api/therapist/children/{e2e_child_id}/link-caregiver?caregiverId={cg1_user_id}&relationship=Mother", headers=headers_therapist)

    # Assign Home Program to Child
    # Get available activity ID 1 (Brushing Teeth)
    r_assign_hp = requests.post(f"{BASE_URL}/api/home-programs", headers=headers_therapist, json={
        "childId": e2e_child_id,
        "activityId": 1,
        "title": "Daily Brushing Practice",
        "description": "Practice morning and night",
        "frequencyPerWeek": 7,
        "startDate": "2026-10-10",
        "endDate": "2026-11-01"
    })
    assert r_assign_hp.status_code == 200, f"Assign home program failed: {r_assign_hp.text}"
    hp_id = r_assign_hp.json()["id"]
    log(f"Assigned Home Program ID {hp_id} to Child {e2e_child_id}.")

    # Inspect Therapist Children List
    r_ch_list = requests.get(f"{BASE_URL}/api/therapist/children", headers=headers_therapist)
    assert r_ch_list.status_code == 200, f"Child list fetch failed: {r_ch_list.text}"
    log(f"Therapist accessed Children List: {len(r_ch_list.json())} children found.")

    # 2.3 CAREGIVER WORKFLOW
    log("\n[2.3] Caregiver Workflow:")
    r_cg_dash = requests.get(f"{BASE_URL}/api/caregiver/dashboard-summary", headers=headers_cg1)
    assert r_cg_dash.status_code == 200, f"Caregiver dash failed: {r_cg_dash.text}"
    cg_dash = r_cg_dash.json()
    log(f"Caregiver Dashboard loaded. Linked children count: {len(cg_dash.get('linkedChildren', []))}")

    # Retrieve Home Programmes
    r_cg_hps = requests.get(f"{BASE_URL}/api/home-programs/today", headers=headers_cg1)
    assert r_cg_hps.status_code == 200, f"Caregiver home programs failed: {r_cg_hps.text}"
    cg_hps = r_cg_hps.json()
    log(f"Caregiver retrieved assigned Home Programmes: {len(cg_hps)} active programs.")

    # Record Caregiver Practice Session with clientSessionId
    cg_session_uuid = str(uuid.uuid4())
    r_practice = requests.post(f"{BASE_URL}/api/home-practice/submit", headers=headers_cg1, json={
        "childId": e2e_child_id,
        "homeProgramId": hp_id,
        "activityId": 1,
        "practiceDate": "2026-10-10",
        "durationMinutes": 15,
        "caregiverNotes": "Child followed steps nicely.",
        "clientSessionId": cg_session_uuid
    })
    assert r_practice.status_code == 200, f"Caregiver practice submission failed: {r_practice.text}"
    practice_id = r_practice.json()["id"]
    log(f"Caregiver submitted Practice Session ID {practice_id} (clientSessionId={cg_session_uuid}).")

    # 2.4 CHILD MODE WORKFLOW
    log("\n[2.4] Child Mode Workflow:")
    r_child_dash = requests.get(f"{BASE_URL}/api/child-mode/dashboard/{e2e_child_id}", headers=headers_cg1)
    assert r_child_dash.status_code == 200, f"Child mode dashboard failed: {r_child_dash.text}"
    c_dash = r_child_dash.json()
    log(f"Child Mode Dashboard loaded for Child {e2e_child_id}: Preferred Name='{c_dash['preferredName']}', Total Stars={c_dash['totalStars']}")

    # Submit Step 1 progress (Partial)
    r_c_p1 = requests.post(f"{BASE_URL}/api/child-mode/progress", headers=headers_cg1, json={
        "childId": e2e_child_id,
        "activityId": 1,
        "stepNumber": 1,
        "isActivityCompleted": False
    })
    assert r_c_p1.status_code == 200, f"Child step 1 progress failed: {r_c_p1.text}"
    log(f"Partial progress submitted: Step 1 complete.")

    # Submit Step 2 completion
    r_c_p2 = requests.post(f"{BASE_URL}/api/child-mode/progress", headers=headers_cg1, json={
        "childId": e2e_child_id,
        "activityId": 1,
        "stepNumber": 2,
        "isActivityCompleted": True
    })
    assert r_c_p2.status_code == 200, f"Child step 2 completion failed: {r_c_p2.text}"
    c_p2_data = r_c_p2.json()
    log(f"Activity completed: isCompleted={c_p2_data['isCompleted']}, starsEarned={c_p2_data['starsEarned']}")

    # Verify Child Rewards
    r_c_rewards = requests.get(f"{BASE_URL}/api/child-mode/rewards/{e2e_child_id}", headers=headers_cg1)
    assert r_c_rewards.status_code == 200, f"Child rewards fetch failed: {r_c_rewards.text}"
    log(f"Child Rewards retrieved: {len(r_c_rewards.json())} badges earned.")

    # ---------------------------------------------------------
    # 3. CROSS-ROLE AUTHORIZATION MATRIX AUDIT
    # ---------------------------------------------------------
    log("\n--- SECTION 3: CROSS-ROLE AUTHORIZATION MATRIX AUDIT ---")

    auth_matrix_results = []

    def audit_endpoint(name, method, url, headers, payload, expected_status):
        if method == "GET":
            r = requests.get(url, headers=headers)
        elif method == "POST":
            r = requests.post(url, headers=headers, json=payload)
        elif method == "PUT":
            r = requests.put(url, headers=headers, json=payload)
        
        status_ok = (r.status_code == expected_status)
        auth_matrix_results.append({
            "endpoint": name,
            "method": method,
            "url": url,
            "expected": expected_status,
            "actual": r.status_code,
            "pass": status_ok
        })
        log(f"[{'PASS' if status_ok else 'FAIL'}] {name} ({method} {url}) -> Expected {expected_status}, Got {r.status_code}")
        return r

    # 3.1 Unauthenticated requests (HTTP 401)
    audit_endpoint("Unauthenticated Admin Dashboard", "GET", f"{BASE_URL}/api/admin/dashboard/stats", {}, None, 401)
    audit_endpoint("Unauthenticated Therapist Dashboard", "GET", f"{BASE_URL}/api/therapist/dashboard-summary", {}, None, 401)
    audit_endpoint("Unauthenticated Caregiver Dashboard", "GET", f"{BASE_URL}/api/caregiver/dashboard-summary", {}, None, 401)

    # 3.2 Caregiver attempting Super Admin route (HTTP 403)
    audit_endpoint("Caregiver accessing Admin Dashboard", "GET", f"{BASE_URL}/api/admin/dashboard/stats", headers_cg1, None, 403)
    audit_endpoint("Caregiver accessing Audit Logs", "GET", f"{BASE_URL}/api/admin/audit-logs", headers_cg1, None, 403)

    # 3.3 Caregiver attempting Therapist route (HTTP 403)
    audit_endpoint("Caregiver creating Child", "POST", f"{BASE_URL}/api/therapist/create-child-with-caregiver", headers_cg1, {"childFirstName": "Illegal"}, 403)

    # 3.4 Caregiver accessing unauthorized child (HTTP 403)
    # Child 24 is assigned to Caregiver 2, not Caregiver 1
    audit_endpoint("Caregiver 1 accessing Unauthorized Child 24 Dashboard", "GET", f"{BASE_URL}/api/child-mode/dashboard/24", headers_cg1, None, 403)
    audit_endpoint("Caregiver 1 submitting progress for Unauthorized Child 24", "POST", f"{BASE_URL}/api/child-mode/progress", headers_cg1, {"childId": 24, "activityId": 1}, 403)

    # 3.5 Invalid Resource ID (HTTP 404)
    audit_endpoint("Admin fetching non-existent Child 999999", "GET", f"{BASE_URL}/api/admin/children/999999", headers_admin, None, 404)

    # ---------------------------------------------------------
    # 4. DATA INTEGRITY & CONCURRENCY REGRESSION AUDIT
    # ---------------------------------------------------------
    log("\n--- SECTION 4: DATA INTEGRITY & CONCURRENCY REGRESSION AUDIT ---")

    # 4.1 Caregiver Practice Session Idempotency (clientSessionId)
    log("\n[4.1] Testing Caregiver Practice Session Idempotency (clientSessionId):")
    idempotent_uuid = str(uuid.uuid4())
    practice_req = {
        "childId": e2e_child_id,
        "homeProgramId": hp_id,
        "activityId": 1,
        "practiceDate": "2026-10-10",
        "durationMinutes": 10,
        "caregiverNotes": "Idempotency test session.",
        "clientSessionId": idempotent_uuid
    }

    # Submit twice sequentially
    r1 = requests.post(f"{BASE_URL}/api/home-practice/submit", headers=headers_cg1, json=practice_req)
    r2 = requests.post(f"{BASE_URL}/api/home-practice/submit", headers=headers_cg1, json=practice_req)
    assert r1.status_code == 200 and r2.status_code == 200, f"Sequential submit failed: r1={r1.text}, r2={r2.text}"
    assert r1.json()["id"] == r2.json()["id"], f"Idempotency failed! Session 1 ID: {r1.json()['id']}, Session 2 ID: {r2.json()['id']}"
    log(f"Sequential Idempotency PASSED -> Both requests returned identical Practice Session ID {r1.json()['id']}.")

    # Submit 10 concurrent requests with SAME clientSessionId
    log(f"Launching 10 concurrent requests with clientSessionId={idempotent_uuid}...")
    def submit_cg_practice(req_payload):
        return requests.post(f"{BASE_URL}/api/home-practice/submit", headers=headers_cg1, json=req_payload)

    with ThreadPoolExecutor(max_workers=10) as executor:
        futures = [executor.submit(submit_cg_practice, practice_req) for _ in range(10)]
        results_cg = [f.result() for f in as_completed(futures)]

    session_ids = [r.json()["id"] for r in results_cg if r.status_code == 200]
    assert len(set(session_ids)) == 1, f"Concurrent practice idempotency failed! Returned distinct session IDs: {set(session_ids)}"
    log(f"Concurrent Practice Idempotency PASSED -> 10 simultaneous threads returned single session ID {session_ids[0]}.")

    # 4.2 Child Progress Concurrency & Rewards Exactly-Once
    log("\n[4.2] Testing Child Progress Concurrency & Exactly-Once Reward Integrity:")
    # Activity 2 completion for Child e2e_child_id
    dash_before = requests.get(f"{BASE_URL}/api/child-mode/dashboard/{e2e_child_id}", headers=headers_cg1).json()
    stars_before = dash_before["totalStars"]

    def submit_child_completion(c_id, a_id):
        return requests.post(f"{BASE_URL}/api/child-mode/progress", headers=headers_cg1, json={
            "childId": c_id,
            "activityId": a_id,
            "stepNumber": 2,
            "isActivityCompleted": True
        })

    log(f"Launching 10 concurrent completion requests for Child {e2e_child_id}, Activity 2...")
    with ThreadPoolExecutor(max_workers=10) as executor:
        futures_ch = [executor.submit(submit_child_completion, e2e_child_id, 2) for _ in range(10)]
        results_ch = [f.result() for f in as_completed(futures_ch)]

    assert all(r.status_code == 200 for r in results_ch), f"Child progress concurrent requests failed: {[r.status_code for r in results_ch]}"
    dash_after = requests.get(f"{BASE_URL}/api/child-mode/dashboard/{e2e_child_id}", headers=headers_cg1).json()
    star_increase = dash_after["totalStars"] - stars_before
    assert star_increase == 5, f"Expected exactly +5 stars increase, got +{star_increase}"
    log(f"Child Progress Concurrency PASSED -> 10 threads awarded exactly +5 stars (Before={stars_before}, After={dash_after['totalStars']}).")

    # Verify Rewards Count for COMPLETION_STAR
    rewards = requests.get(f"{BASE_URL}/api/child-mode/rewards/{e2e_child_id}", headers=headers_cg1).json()
    star_badges = [r for r in rewards if r.get("badgeType") == "COMPLETION_STAR"]
    assert len(star_badges) == 1, f"Expected exactly 1 COMPLETION_STAR badge, found {len(star_badges)}"
    log("Exactly-Once Reward Integrity PASSED -> Database contains exactly 1 COMPLETION_STAR badge.")

    # ---------------------------------------------------------
    # 5. FINAL SUMMARY
    # ---------------------------------------------------------
    log("\n=== STEP 5 E2E INTEGRATION & AUDIT TEST SUITE COMPLETED SUCCESSFULLY ===")

if __name__ == "__main__":
    main()
