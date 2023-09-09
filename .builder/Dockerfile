FROM ubuntu:20.04

LABEL version="1.0"
LABEL description="Install any package using SDKMan"

ARG JAVA_VERSION="17.0.8-graal"
ARG MAVEN_VERSION="3.9.4"


ENV SDKMAN_DIR /root/.sdkman

RUN apt-get update
RUN rm /bin/sh && ln -s /bin/bash /bin/sh
RUN apt-get -qq -y install curl wget unzip zip git

# install docker
RUN docker_cli_package=$(curl -fLsS https://download.docker.com/linux/static/stable/$(uname -m)/ | grep -oP '(?<=>)docker-\d+.\d+.\d+.tgz(?=</a>)' | tail -1) \
    && docker_cli_download_url=https://download.docker.com/linux/static/stable/$(uname -m)/$docker_cli_package \
    && echo "Downloading [$docker_cli_download_url]..." \
    && curl -fL $docker_cli_download_url | tar zxv -C /usr/bin --strip-components=1 docker/docker


# install sdkman , java and maven
RUN curl -s "https://get.sdkman.io" | bash
RUN set -x \
    && echo "sdkman_auto_answer=true" > $SDKMAN_DIR/etc/config \
    && echo "sdkman_auto_selfupdate=false" >> $SDKMAN_DIR/etc/config \
    && echo "sdkman_insecure_ssl=false" >> $SDKMAN_DIR/etc/config

RUN source "$HOME/.sdkman/bin/sdkman-init.sh" \
    && sdk install java $JAVA_VERSION \
    && sdk install maven $MAVEN_VERSION \
    && rm -rf $HOME/.sdkman/archives/* \
    && rm -rf $HOME/.sdkman/tmp/* \
    && mvn --version

ENV PATH=$HOME/.sdkman/candidates/java/current/bin:$PATH
ENV PATH=$HOME/.sdkman/candidates/maven/current/bin:$PATH
