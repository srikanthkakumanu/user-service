# User Service

An example spring boot microservice to demonstrate spring security.

This microservice followed ***buildpack*** approach (without using Dockerfile) to build OCI images and to push the image to Docker registry.

## 1. Building and Publishing OCI Images

### 1.1 Using Spring Boot Gradle Plugin (bootBuildImage)

* **Using Command line Args:-** We can run the following command to build the docker image (It uses Paketo internally), by passing project properties (-P) and publish it to DockerHub:

```
./gradlew bootBuildImage --publishImage -Pdcr_username=dockerhubusername -Pdcr_password=dockerhubpassword -Pdcr_repo_path=dockerrepopath
```

Example: `./gradlew bootBuildImage --publishImage -Pdcr_username=username -Pdcr_password=password -Pdcr_repo_path=srik1980`


* **Using gradle.properties file:-** We can create a gradle.properties file under project root directory and set these project properties in *key=value* convention.

Example:

**gradle.properties (File)**

---

```
dcr_repo_path=dockerhubrepopath
dcr_username=username
dcr_password=password
```

### 1.2 Build a container image (Using Paketo)

This service uses Paketo buildpacks to generate a OCI container image.

To set default packeto builder:

```
pack config default-builder paketobuildpacks/builder-jammy-tiny
```

or, it can also explicitly can be set

```
pack build user-service -B packetobuildpacks/builder-jammy-tiny
```

To change JDK type and version


## 2. Running the image
