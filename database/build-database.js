const fs = require("fs");
const path = require("path");
const { DatabaseSync } = require("node:sqlite");

const directory = __dirname;
const databasePath = path.join(directory, "LopHocLapTrinh.db");
const seedPath = path.join(directory, "seed.sql");
fs.rmSync(databasePath, { force: true });
const database = new DatabaseSync(databasePath);

database.exec(fs.readFileSync(seedPath, "utf8"));

const counts = database.prepare(
    "SELECT " +
    "(SELECT COUNT(*) FROM students) students," +
    "(SELECT COUNT(*) FROM courses) courses," +
    "(SELECT COUNT(*) FROM enrollments) enrollments"
).get();
const integrity = database.prepare("PRAGMA integrity_check").get();
const foreignKeyViolations = database.prepare("PRAGMA foreign_key_check").all();
const report = database.prepare(
    "SELECT COUNT(DISTINCT s.id) total " +
    "FROM students s " +
    "JOIN enrollments e ON e.student_id=s.id " +
    "JOIN courses c ON c.id=e.course_id " +
    "WHERE s.age BETWEEN 10 AND 12 " +
    "AND c.language='Python' AND c.level='Cơ bản'"
).get();
const timeReport = database.prepare(
    "SELECT COUNT(*) total FROM enrollments " +
    "WHERE date(enrolled_at) BETWEEN date('2026-06-01') AND date('2026-08-31')"
).get();
const version = database.prepare("PRAGMA user_version").get();

console.log(JSON.stringify({
    counts,
    integrity,
    foreignKeyViolations: foreignKeyViolations.length,
    pythonBasicAge10To12: report.total,
    enrollmentsFromJuneToAugust2026: timeReport.total,
    userVersion: version.user_version
}, null, 2));

database.close();
