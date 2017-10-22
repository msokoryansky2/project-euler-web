FROM openjdk:8u141-slim
COPY target/universal/ /usr/
WORKDIR /usr/
RUN unzip project-euler-web-1.0-SNAPSHOT
RUN chmod +x /usr/project-euler-web-1.0-SNAPSHOT/bin
EXPOSE 9000
CMD ["/usr/project-euler-web-1.0-SNAPSHOT/bin/project-euler-web", "-J-XX:+UnlockExperimentalVMOptions", "-J-XX:+UseCGroupMemoryLimitForHeap"]

# #######################################################
# Create Docker image:
# --------------------
# build -t project-euler-web .
#
#
# Run Docker image:
# -----------------
# run -p 9000:9000 project-euler-web
# #######################################################