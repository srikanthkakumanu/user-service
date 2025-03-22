# User Service

An example spring boot microservice to demonstrate spring security.

This microservice can follow ***buildpack*** approach (without using Dockerfile) to build OCI images and to push the image to Docker registry.

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


### 3. Run

---

The following command builds an image and tags it as srikanthkakumanu/user-service and runs the Docker image locally. The build creates a spring user and spring group to run the application.

``````bash

docker build --build-arg JAR_FILE=build/libs/user-service-1.0.jar -t srikanthkakumanu/user-service .
``````

Run the application with user privileges helps to mitigate some risks. So, an important improvement to the Dockerfile is to run the application as a non-root user.

### Build Docker Image

---

```bash

./gradlew bootBuildImage --imageName=srikanthkakumanu/user-service
```

or

```bash
docker build -t srikanthkakumanu/user-service:1.0 .
```

### Push Docker Image to DockerHub

---

```bash

docker image push srikanthkakumanu/user-service:1.0
```

### Using Spring Profiles

---

```bash

docker run -e "SPRING_PROFILES_ACTIVE=prod" -p 8080:8080 -t srikanthkakumanu/user-service
```

or

```bash
docker run -e "SPRING_PROFILES_ACTIVE=dev" -p 8080:8080 -t srikanthkakumanu/user-service
```

### Debug App in Docker container (using JPDA)

---

```bash
docker run -e "JAVA_TOOL_OPTIONS=-agentlib:jdwp=transport=dt_socket,address=5005,server=y,suspend=n" -p 8080:8080 -p 5005:5005 -t srikanthkakumanu/user-service
```
