DROP TABLE IF EXISTS history_entries ;
DROP TABLE IF EXISTS schedule_entries ;
DROP TABLE IF EXISTS weeks ;
DROP TABLE IF EXISTS days ;


CREATE TABLE "days" (
	"id"	INTEGER NOT NULL UNIQUE,
	"name"	TEXT,
	PRIMARY KEY("id")
) ;

CREATE TABLE "weeks" (
	"id"	INTEGER NOT NULL UNIQUE,
	"week_number"	INTEGER,
	PRIMARY KEY("id" AUTOINCREMENT)
) ;


CREATE TABLE "schedule_entries" (
	"id"	INTEGER NOT NULL UNIQUE,
	"is_going_to_stream"	INTEGER,
	"time_comment"	TEXT,
	"date_time"	INTEGER,
	"comment"	TEXT,
	"day_id"	INTEGER NOT NULL ,
	"week_id"	INTEGER NOT NULL ,
	PRIMARY KEY("id" AUTOINCREMENT),
	FOREIGN KEY("day_id") REFERENCES "days"("id"),
	FOREIGN KEY("week_id") REFERENCES "weeks"("id")
) ;


CREATE TABLE "history_entries" (
	"id"	INTEGER NOT NULL UNIQUE,
	"timestamp"	INTEGER,
	"user_name"	TEXT,
	"interaction"	TEXT,
	PRIMARY KEY("id" AUTOINCREMENT)
) ;
