FROM ubuntu:latest As build
RUN apt-get update
RUN apt-get install open-jdk-17-jdk -y
COPY . .


ENTRYPOINT ["top", "-b"]