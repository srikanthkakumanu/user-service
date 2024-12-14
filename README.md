# User Service

An example spring boot microservice to demonstrate spring security.

This microservice followed ***buildpack*** approach (without using Dockerfile) to build OCI images and to push the image to Docker registry.

Ref: [pack and kpack](https://kube.academy/courses/building-images/lessons/building-images-with-buildpacks-pack-spring-boot-kpack-and-paketo-buildpacks)

## 1. Building and Publishing OCI Images

### 1.1 Using Spring Boot Gradle Plugin (bootBuildImage)

* **Using Command line Args:-** We can run the following command to build the docker image (It uses Paketo internally), by passing project properties (-P) and publish it to DockerHub:

```
.
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

### 1.2 Using Paketo(externally via CLI)

We can also use Paketo buildpacks externally to generate a OCI container image.

```
pack build userDomain-service --builder bellsoft/buildpacks.builder:musl --env BP_JVM_VERSION=21 --env  BP_NATIVE_IMAGE=true
docker tag userDomain-service johndoe/userDomain-service:1.0
docker login
docker push johndoe/userDomain-service:1.0
```

## 2. Build and Run from Compose file

Note: Dockerfile should be present to build and run the customized image.

```
./gradlew build
docker compose up or docker compose up --build
```
