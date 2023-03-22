ARG image
FROM ${image}

COPY *.sh /docker-entrypoint-initdb.d/
COPY *.sql /docker-entrypoint-initdb.d/
COPY postgresql.conf.sample /usr/share/postgresql/postgresql.conf.sample
