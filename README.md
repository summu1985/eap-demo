# JBoss EAP 7.x on OpenShift — WAR-Only, Kubernetes-Native Deployment

This repository demonstrates a **practical, supported approach** to deploying **legacy JBoss EAP 7.x applications on Red Hat OpenShift** when:

- Application code cannot be modified
- Only a prebuilt WAR file is available
- Customers want a **low-risk path toward JBoss EAP 8**

The demo uses **JBoss EAP OpenShift S2I binary builds** to assemble a runtime image without rebuilding application source code.

---

## 🎥 Full Demo Walkthrough

▶ **YouTube video (end-to-end demo):**  
https://youtu.be/vMosdVSpFGA

📢 **LinkedIn carousel post:**  


---

## What This Demo Covers

- WAR-only deployment using **JBoss EAP OpenShift S2I**
- No Maven build, no application source required
- JDBC drivers installed as **JBoss modules**
- Datasource configuration externalized via environment variables
- Runtime database switching (PostgreSQL ↔ MySQL)
- Two configuration strategies:
  - **Full configuration copy**
  - **Postconfigure CLI scripts**

---

## Repository Structure (Key Directories)

```
binary-input/
├── deployments/
│   └── eap-ds-demo.war
├── extensions/
│   ├── install.sh
│   ├── drivers.env
│   ├── modules/
│   │   ├── org/postgresql/
│   │   └── com/mysql/
│   ├── postconfigure.sh
│   └── enable-app-debug.cli
```

### Key Concepts
- **deployments/** → WAR injected during binary S2I build  
- **extensions/** → Server customizations using supported S2I hooks  
- **modules/** → JDBC drivers as server modules  
- **postconfigure.sh** → Runtime configuration using JBoss CLI  

---

## Branches Explained

### `full-config`
Use this when:
- Existing EAP configuration is heavily customized
- You want the fastest path to containerization
- Full server configuration can be copied as-is

### `postconfigure`
Use this when:
- Customizations are small and controlled
- You want a Kubernetes-native approach
- Preparing for JBoss EAP 8

Both approaches are valid and supported.

---

## Prerequisites

- OpenShift cluster (4.x)
- Access to Red Hat container registry
- JBoss EAP 7.4 OpenShift image
- PostgreSQL and MySQL running in the cluster (for DB switch demo)
- `oc` CLI installed and logged in

---

## Step-by-Step Demo Execution

### 1️⃣ Create a Binary Build
```bash
oc new-build --binary=true \
  --name=eap-ds-demo \
  --image=registry.redhat.io/jboss-eap-7/eap74-openjdk11-openshift-rhel8
```

### 2️⃣ Configure S2I Environment Variables
```bash
oc set env bc/eap-ds-demo ARTIFACT_DIR=deployments
oc set env bc/eap-ds-demo CUSTOM_INSTALL_DIRECTORIES=extensions
```

### 3️⃣ Start the Binary Build
```bash
oc start-build eap-ds-demo --from-dir=binary-input --follow
```

---

### 4️⃣ Deploy the Application
```bash
oc new-app eap-ds-demo --name=eap-ds-demo
oc expose svc/eap-ds-demo
```

---

### 5️⃣ Configure PostgreSQL Datasource
```bash
oc set env deploy/eap-ds-demo \
DB_SERVICE_PREFIX_MAPPING=app-postgresql=APP \
APP_JNDI=java:jboss/datasources/AppDS \
APP_URL='jdbc:postgresql://postgresql:5432/demo?sslmode=disable' \
APP_USERNAME=demo \
APP_PASSWORD=demo \
APP_NONXA=true
```

Access the app:
```bash
APP_URL="http://$(oc get route eap-ds-demo -o jsonpath='{.spec.host}')/eap-ds-demo/hello"
echo $APP_URL
```

---

### 6️⃣ Switch to MySQL at Runtime
```bash
oc set env deploy/eap-ds-demo \
DB_SERVICE_PREFIX_MAPPING=app-mysql=APP \
APP_JNDI=java:jboss/datasources/AppDS \
APP_URL='jdbc:mysql://mysql:3306/demo' \
APP_USERNAME=demo \
APP_PASSWORD=demo \
APP_NONXA=true
```

No rebuild. No code change.

---

### 7️⃣ Configuration Customization Demo

- **Full config branch** → rebuild image with modified configuration
- **Postconfigure branch** → apply logging changes via CLI at runtime

Verify:
```bash
oc logs deploy/eap-ds-demo | grep DEBUG
```

---

## Why This Approach Matters

Instead of a risky **big-bang migration**:
- Move EAP 7 workloads to OpenShift first
- Decouple platform from application
- Reduce operational and migration risk
- Prepare cleanly for JBoss EAP 8

---

## Final Thought

Modernization doesn’t always start with rewriting applications.  
Sometimes, the biggest impact comes from **changing how applications are deployed and operated**.
