FROM ubuntu:latest
LABEL authors="oscar"

ENTRYPOINT ["top", "-b"]