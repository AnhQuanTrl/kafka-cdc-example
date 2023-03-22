ARG image
FROM ${image}

## Install connectors
RUN echo "\nInstalling all required connectors...\n" && \
confluent-hub install --no-prompt confluentinc/kafka-connect-jdbc:latest && \
confluent-hub install --no-prompt debezium/debezium-connector-postgresql:latest && \
confluent-hub install --no-prompt confluentinc/connect-transforms:latest
