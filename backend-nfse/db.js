const { Pool } = require("pg");

const pool = new Poll({
    user: "postgres",
    host: "localhost",
    database: "nfse_db",
});

module.exports = pool;