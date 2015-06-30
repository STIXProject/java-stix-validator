############################################################
# A Dockerfile used to create a java-stix build container
# based on Ubunu.
#
# Copyright (c) 2015, The MITRE Corporation. All rights reserved.
# See LICENSE for complete terms.
#
# @author nemonik (Michael Joseph Walsh <github.com@nemonik.com>)
#
# WHAT TO DO:
#
# If you have Docker installed, from the root of the project run
# the following to create a container image for this Dockerfile via:
#
# docker build -t nemonik/validator .
#
# With the image is built, start a container with:
#
# docker run -d -p 8080:8080 nemonik/validator
#
# If you're using boot2docker, you will need to access via VM’s 
# host only interface IP address:
#
# boot2docker ip
#
# And then point your browser to http://$(boot2docker ip):8080 
# vice https://localhost:8080
############################################################

# Set base image
FROM java:8

# File Maintainer
MAINTAINER Michael Joseph Walsh

# Update
RUN apt-get -y update

# Cannot run bower as root so we will need to install as another user
RUN adduser --disabled-password --gecos '' nemonik  && adduser nemonik sudo && echo '%sudo ALL=(ALL) NOPASSWD:ALL' >> /etc/sudoers

# Copy java-stix-validator project into the container
WORKDIR /home/stix
COPY . java-stix-validator
RUN chown -R stix java-stix-validator
WORKDIR /home/stix/java-stix-validator

# Build the project
RUN su stix -c './gradlew stage'

# Expose and start the validator microservice
EXPOSE 8080
CMD ["/home/stix/java-stix-validator/build/install/java-stix-validator/bin/java-stix-validator"]