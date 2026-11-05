#===============================================================================
# Tandem Tales Agent (Java) Docker Image
# 
# This Dockerfile defines a container (a lightweight virtual machine) that
# includes an operating system, the Java Virtual Machine and Java Development
# Kit, the Tandem Tales Java library, and the source code for a Tandem Tales
# agent.
#===============================================================================

#-------------------------------------------------------------------------------
# Base Phase
# The instructions in this section always run.
#-------------------------------------------------------------------------------
# Start with Linux that has Java already installed.
FROM eclipse-temurin:26-jdk-resolute AS base

# Update packages.
RUN apt update
# Git downloads the Tandem Tales Java library.
RUN apt install -y git

# Install the Tandem Tales Java Client library from GitHub.
RUN git clone https://github.com/sgware/tt-client-java.git /tmp/tt-client-java
# Install the Story Graph library from GitHub.
RUN git clone https://github.com/sgware/story-graph.git /tmp/story-graph
RUN mkdir -p /opt/tt
RUN cp /tmp/tt-client-java/build/jar/tt-client-java.jar /opt/tt/tt-client-java.jar
RUN cp /tmp/story-graph/build/jar/story-graph.jar /opt/tt/story-graph.jar
RUN rm -rf /tmp/*

# Uninstall software and clean up.
RUN apt purge -y git
RUN apt autoremove -y
RUN apt clean
RUN rm -rf /var/lib/apt/lists/*

# Copy the agent's files into the image.
COPY ./root /

# Set `/app` as the working directory.
WORKDIR /app

# Compile the agent.
RUN ./compile.sh

#-------------------------------------------------------------------------------
# Development Phase
# The instructions in this section only run if you build the image with
# `--target dev`. Put instructions here that should only run when you are
# testing and developing the image. This section assumes there is a container
# named `tt-web` running as a test version of the Tandem Tales web server.
#-------------------------------------------------------------------------------
FROM base AS dev

# Run the development version of the entrypoint script and keep the shell open.
CMD ["sh", "-c", "trap 'exec bash' INT; /app/run.dev.sh; exec bash"]

#-------------------------------------------------------------------------------
# Production Phase
# The instructions in this section run if you build the image with
# `--target prod`. Since this is the last phase defined, these instructions will
# run by default if you don't specify a target. Put instructions here that
# should only run in the final, working version of your image.
#-------------------------------------------------------------------------------
FROM base AS prod

# Run the production version of the entrypoint script.
CMD ["sh", "-c", "/app/run.prod.sh"]