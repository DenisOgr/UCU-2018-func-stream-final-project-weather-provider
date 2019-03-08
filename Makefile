DOCKER_IMAGE_NAME = ucu.edu.ua/denis_porplenko:2.12.8


.PHONY: .build
build:
	docker build . -t $(DOCKER_IMAGE_NAME)

.PHONY: .assembly
assembly:
	docker run --rm  -it -v  "${PWD}":/app  -w /app ucu.edu.ua/denis_porplenko:2.12.8 bash ./assembly.sh



