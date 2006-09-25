-- CVS ID: $Id$
 
SET FOREIGN_KEY_CHECKS=0;
drop table if exists tl_lascrb11_attachment;
drop table if exists tl_lascrb11_heading;
drop table if exists tl_lascrb11_scribe;
drop table if exists tl_lascrb11_session;
drop table if exists tl_lascrb11_user;
create table tl_lascrb11_attachment (uid bigint not null auto_increment, file_version_id bigint, file_type varchar(255), file_name varchar(255), file_uuid bigint, create_date datetime, scribe_uid bigint, primary key (uid));
create table tl_lascrb11_heading (uid bigint not null auto_increment, heading text, report text, scribe_uid bigint, display_order integer, primary key (uid));
create table tl_lascrb11_scribe (uid bigint not null auto_increment, create_date datetime, update_date datetime, create_by bigint, title varchar(255), instructions text, run_offline bit, lock_on_finished bit, auto_select_scribe bit, reflect_on_activity bit, reflect_instructions text, online_instructions text, offline_instructions text, content_in_use bit, define_later bit, tool_content_id bigint, primary key (uid));
create table tl_lascrb11_session (uid bigint not null auto_increment, session_end_date datetime, session_start_date datetime, status integer, session_id bigint, session_name varchar(250), scribe_uid bigint, primary key (uid));
create table tl_lascrb11_user (uid bigint not null auto_increment, user_id bigint, last_name varchar(255), login_name varchar(255), first_name varchar(255), finishedActivity bit, scribe_session_uid bigint, primary key (uid));
alter table tl_lascrb11_attachment add index FK57953706B3FA1495 (scribe_uid), add constraint FK57953706B3FA1495 foreign key (scribe_uid) references tl_lascrb11_scribe (uid);
alter table tl_lascrb11_heading add index FK428A22FFB3FA1495 (scribe_uid), add constraint FK428A22FFB3FA1495 foreign key (scribe_uid) references tl_lascrb11_scribe (uid);
alter table tl_lascrb11_session add index FK89732793B3FA1495 (scribe_uid), add constraint FK89732793B3FA1495 foreign key (scribe_uid) references tl_lascrb11_scribe (uid);
alter table tl_lascrb11_user add index FK187DAFEE1C266FAE (scribe_session_uid), add constraint FK187DAFEE1C266FAE foreign key (scribe_session_uid) references tl_lascrb11_session (uid);

INSERT INTO tl_lascrb11_scribe (
	title,
	instructions,
	online_instructions,
	offline_instructions,
	tool_content_id,
	run_offline,
	lock_on_finished,
	content_in_use,
	define_later,
	reflect_on_activity,
	auto_select_scribe) 
VALUES(
	"LAMS Scribe",
	"Scribe Instruction",
	"Online instructions",
	"Offline instructions",
	${default_content_id},
	0,
	1,
	0,
	0,
	0,
	0
);

SET FOREIGN_KEY_CHECKS=1;
