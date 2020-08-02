-- --------------------------------------------------------
-- Хост:                         127.0.0.1
-- Версия сервера:               5.6.24 - MySQL Community Server (GPL)
-- ОС Сервера:                   Win32
-- HeidiSQL Версия:              9.1.0.4940
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

-- Дамп структуры для таблица offlinedb.acl
CREATE TABLE IF NOT EXISTS `acl` (
  `actor` varchar(255) NOT NULL,
  `ouid` varchar(255) NOT NULL,
  `permissions` int(11) DEFAULT NULL,
  PRIMARY KEY (`actor`,`ouid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Дамп данных таблицы offlinedb.acl: ~8 rows (приблизительно)
/*!40000 ALTER TABLE `acl` DISABLE KEYS */;
INSERT INTO `acl` (`actor`, `ouid`, `permissions`) VALUES
	('ROLE_USER', 'c:::FederalService', 1),
	('ROLE_USER', 'c:::Petition', 1),
	('ROLE_USER', 'c:::RegionalService', 1),
	('ROLE_USER', 'c:::SmevEntity', 1),
	('ROLE_USER', 'n:::all', 1),
	('ROLE_USER', 'n:::fes', 1),
	('ROLE_USER', 'n:::petitions', 1),
	('ROLE_USER', 'n:::res', 1);
/*!40000 ALTER TABLE `acl` ENABLE KEYS */;


-- Дамп структуры для таблица offlinedb.authority
CREATE TABLE IF NOT EXISTS `authority` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` bigint(20) NOT NULL,
  `authority` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `authority` (`authority`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

-- Дамп данных таблицы offlinedb.authority: ~2 rows (приблизительно)
/*!40000 ALTER TABLE `authority` DISABLE KEYS */;
INSERT INTO `authority` (`id`, `version`, `authority`) VALUES
	(1, 0, 'ROLE_USER'),
	(2, 0, 'ROLE_ADMIN');
/*!40000 ALTER TABLE `authority` ENABLE KEYS */;


-- Дамп структуры для таблица offlinedb.changelog
CREATE TABLE IF NOT EXISTS `changelog` (
  `actor` varchar(255) NOT NULL,
  `object_class` varchar(255) NOT NULL,
  `object_id` varchar(255) NOT NULL,
  `time` datetime NOT NULL,
  `type` varchar(255) NOT NULL,
  `attr_updates` text,
  PRIMARY KEY (`actor`,`object_class`,`object_id`,`time`,`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Дамп данных таблицы offlinedb.changelog: ~0 rows (приблизительно)
/*!40000 ALTER TABLE `changelog` DISABLE KEYS */;
/*!40000 ALTER TABLE `changelog` ENABLE KEYS */;


-- Дамп структуры для таблица offlinedb.databasechangelog
CREATE TABLE IF NOT EXISTS `databasechangelog` (
  `ID` varchar(255) NOT NULL,
  `AUTHOR` varchar(255) NOT NULL,
  `FILENAME` varchar(255) NOT NULL,
  `DATEEXECUTED` datetime NOT NULL,
  `ORDEREXECUTED` int(11) NOT NULL,
  `EXECTYPE` varchar(10) NOT NULL,
  `MD5SUM` varchar(35) DEFAULT NULL,
  `DESCRIPTION` varchar(255) DEFAULT NULL,
  `COMMENTS` varchar(255) DEFAULT NULL,
  `TAG` varchar(255) DEFAULT NULL,
  `LIQUIBASE` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Дамп данных таблицы offlinedb.databasechangelog: ~0 rows (приблизительно)
/*!40000 ALTER TABLE `databasechangelog` DISABLE KEYS */;
/*!40000 ALTER TABLE `databasechangelog` ENABLE KEYS */;


-- Дамп структуры для таблица offlinedb.databasechangeloglock
CREATE TABLE IF NOT EXISTS `databasechangeloglock` (
  `ID` int(11) NOT NULL,
  `LOCKED` bit(1) NOT NULL,
  `LOCKGRANTED` datetime DEFAULT NULL,
  `LOCKEDBY` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Дамп данных таблицы offlinedb.databasechangeloglock: ~0 rows (приблизительно)
/*!40000 ALTER TABLE `databasechangeloglock` DISABLE KEYS */;
/*!40000 ALTER TABLE `databasechangeloglock` ENABLE KEYS */;


-- Дамп структуры для таблица offlinedb.not_read
CREATE TABLE IF NOT EXISTS `not_read` (
  `guid` varchar(255) NOT NULL,
  PRIMARY KEY (`guid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Дамп данных таблицы offlinedb.not_read: ~0 rows (приблизительно)
/*!40000 ALTER TABLE `not_read` DISABLE KEYS */;
/*!40000 ALTER TABLE `not_read` ENABLE KEYS */;


-- Дамп структуры для таблица offlinedb.searchtable
CREATE TABLE IF NOT EXISTS `searchtable` (
  `ID` varchar(100) NOT NULL,
  `SEARCHTEXT` text,
  PRIMARY KEY (`ID`),
  FULLTEXT KEY `SEARCHTEXT` (`SEARCHTEXT`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- Дамп данных таблицы offlinedb.searchtable: 0 rows
/*!40000 ALTER TABLE `searchtable` DISABLE KEYS */;
/*!40000 ALTER TABLE `searchtable` ENABLE KEYS */;


-- Дамп структуры для таблица offlinedb.t_federal_service
CREATE TABLE IF NOT EXISTS `t_federal_service` (
  `f_guid` varchar(36) NOT NULL,
  `f_ouid` bigint(20) DEFAULT NULL,
  `f_rqst_org` varchar(36) DEFAULT NULL,
  `f_rqst_person` bigint(20) DEFAULT NULL,
  `f_outcom_numb` varchar(200) DEFAULT NULL,
  `f_answ_date` date DEFAULT NULL,
  `f_type_code` varchar(200) DEFAULT NULL,
  `f_date` date DEFAULT NULL,
  `f_signature` varchar(200) DEFAULT NULL,
  `f_test_msg` bit(1) DEFAULT NULL,
  `f_incom_numb` varchar(200) DEFAULT NULL,
  `f_answ_person` bigint(20) DEFAULT NULL,
  `f_federal_code` varchar(200) DEFAULT NULL,
  `f_status_rqst` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`f_guid`),
  CONSTRAINT `fk_federalservice_guid` FOREIGN KEY (`f_guid`) REFERENCES `t_smev_entity` (`f_guid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Дамп данных таблицы offlinedb.t_federal_service: ~0 rows (приблизительно)
/*!40000 ALTER TABLE `t_federal_service` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_federal_service` ENABLE KEYS */;


-- Дамп структуры для таблица offlinedb.t_petition
CREATE TABLE IF NOT EXISTS `t_petition` (
  `f_guid` varchar(36) NOT NULL,
  `f_resolution` varchar(36) DEFAULT NULL,
  `f_state` bigint(20) DEFAULT NULL,
  `f_number` varchar(200) DEFAULT NULL,
  `f_recipient_org` varchar(36) DEFAULT NULL,
  `f_executor` bigint(20) DEFAULT NULL,
  `f_start_date` date DEFAULT NULL,
  `f_login` varchar(200) DEFAULT NULL,
  `f_pet_code` varchar(200) DEFAULT NULL,
  `f_ecp` text,
  `f_case_number` varchar(200) DEFAULT NULL,
  `f_history` char(1) DEFAULT NULL,
  PRIMARY KEY (`f_guid`),
  KEY `FKD3F594A9771CD89F` (`f_guid`),
  CONSTRAINT `FKD3F594A9771CD89F` FOREIGN KEY (`f_guid`) REFERENCES `t_smev_entity` (`f_guid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Дамп данных таблицы offlinedb.t_petition: ~0 rows (приблизительно)
/*!40000 ALTER TABLE `t_petition` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_petition` ENABLE KEYS */;


-- Дамп структуры для таблица offlinedb.t_regional_service
CREATE TABLE IF NOT EXISTS `t_regional_service` (
  `f_guid` varchar(36) NOT NULL,
  `f_ouid` bigint(20) DEFAULT NULL,
  `f_fsmev_sender` varchar(200) DEFAULT NULL,
  `f_duplicate` varchar(36) DEFAULT NULL,
  `f_oktmo` varchar(200) DEFAULT NULL,
  `f_status_rqst` varchar(200) DEFAULT NULL,
  `f_rqst_org` varchar(36) DEFAULT NULL,
  `f_rqst_org_code` varchar(200) DEFAULT NULL,
  `f_pett_org` varchar(36) DEFAULT NULL,
  `f_new_comment` text,
  `f_app_data` text,
  `f_request_id_ref` varchar(200) DEFAULT NULL,
  `f_comments` varchar(200) DEFAULT NULL,
  `f_origin_request_id_ref` varchar(200) DEFAULT NULL,
  `f_status_type` varchar(200) DEFAULT NULL,
  `f_code_link` varchar(200) DEFAULT NULL,
  `f_answ_person_20150405002450` bigint(20) DEFAULT NULL,
  `f_signature` varchar(200) DEFAULT NULL,
  `f_error_description` varchar(3000) DEFAULT NULL,
  `f_answ_date` date DEFAULT NULL,
  `f_rqst_person` bigint(20) DEFAULT NULL,
  `f_ping_date` date DEFAULT NULL,
  `f_outer_id` varchar(200) DEFAULT NULL,
  `f_answ_person` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`f_guid`),
  KEY `FKB74FD0054DB2CD9` (`f_guid`),
  CONSTRAINT `FKB74FD0054DB2CD9` FOREIGN KEY (`f_guid`) REFERENCES `t_smev_entity` (`f_guid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Дамп данных таблицы offlinedb.t_regional_service: ~0 rows (приблизительно)
/*!40000 ALTER TABLE `t_regional_service` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_regional_service` ENABLE KEYS */;


-- Дамп структуры для таблица offlinedb.t_smev_entity
CREATE TABLE IF NOT EXISTS `t_smev_entity` (
  `f_ouid` bigint(20) DEFAULT NULL,
  `f_guid` varchar(36) NOT NULL,
  `f_rqst_org` varchar(36) DEFAULT NULL,
  `f_outcom_numb` varchar(200) DEFAULT NULL,
  `f_answ_date` date DEFAULT NULL,
  `f_time_stamp` date DEFAULT NULL,
  `f_create_date` date DEFAULT NULL,
  `f_type_code` varchar(200) DEFAULT NULL,
  `f_date` date DEFAULT NULL,
  `f_pett_id` varchar(36) DEFAULT NULL,
  `f_pett_org` varchar(36) DEFAULT NULL,
  `f_test_msg` bit(1) DEFAULT NULL,
  `f_incom_numb` varchar(200) DEFAULT NULL,
  `f_answ_person` bigint(20) DEFAULT NULL,
  `f_federal_code` varchar(200) DEFAULT NULL,
  `f_rqst_person` bigint(20) DEFAULT NULL,
  `f_status_rqst` varchar(200) DEFAULT NULL,
  `_type` varchar(200) DEFAULT NULL,
  `f_date__of__birth` datetime DEFAULT NULL,
  `f_document__name` varchar(200) DEFAULT NULL,
  `f_document__number` varchar(200) DEFAULT NULL,
  `f_dptcodlink` longtext,
  `f_error__code` varchar(200) DEFAULT NULL,
  `f_error__name` varchar(200) DEFAULT NULL,
  `f_first__name` varchar(200) DEFAULT NULL,
  `f_patronymic` varchar(200) DEFAULT NULL,
  `f_pension__date` datetime DEFAULT NULL,
  `f_qrynmb` varchar(200) DEFAULT NULL,
  `f_second__name` varchar(200) DEFAULT NULL,
  `f_snils` varchar(200) DEFAULT NULL,
  `f_statuscode` varchar(200) DEFAULT NULL,
  `f_statusdescr` varchar(200) DEFAULT NULL,
  `f_type__query` varchar(200) DEFAULT NULL,
  `f_birth_date` varchar(200) DEFAULT NULL,
  `f_case_number` varchar(200) DEFAULT NULL,
  `f_code` varchar(200) DEFAULT NULL,
  `f_date_presence` varchar(200) DEFAULT NULL,
  `f_dep_name` varchar(200) DEFAULT NULL,
  `f_dep_region` longtext,
  `f_doc_num` varchar(200) DEFAULT NULL,
  `f_doc_type` varchar(200) DEFAULT NULL,
  `f_fio` varchar(200) DEFAULT NULL,
  `f_firstname` varchar(200) DEFAULT NULL,
  `f_form_date` datetime DEFAULT NULL,
  `f_from_system_link` varchar(38) DEFAULT NULL,
  `f_initial_reg_number` varchar(200) DEFAULT NULL,
  `f_ins_num` varchar(200) DEFAULT NULL,
  `f_lastname` varchar(200) DEFAULT NULL,
  `f_msg__vid` varchar(200) DEFAULT NULL,
  `f_name` varchar(200) DEFAULT NULL,
  `f_num_pays` varchar(200) DEFAULT NULL,
  `f_reasonlink` varchar(200) DEFAULT NULL,
  `f_reg_number` varchar(200) DEFAULT NULL,
  `f_reg_time` datetime DEFAULT NULL,
  `f_reject_info` varchar(200) DEFAULT NULL,
  `f_resolution` varchar(200) DEFAULT NULL,
  `f_result_data` longtext,
  `f_secondname` varchar(200) DEFAULT NULL,
  `f_firmget` varchar(200) DEFAULT NULL,
  `f_firmget__full` varchar(200) DEFAULT NULL,
  `f_firmget__short` varchar(200) DEFAULT NULL,
  `f_inn` varchar(200) DEFAULT NULL,
  `f_is_pril` tinyint(1) DEFAULT NULL,
  `f_mode` tinyint(1) DEFAULT NULL,
  `f_numb` varchar(200) DEFAULT NULL,
  `f_ogrn` varchar(200) DEFAULT NULL,
  `f_row_count` varchar(200) DEFAULT NULL,
  `f_row_overflow` varchar(200) DEFAULT NULL,
  `f_search_mode` tinyint(1) DEFAULT NULL,
  `f_serialnumb` varchar(200) DEFAULT NULL,
  `f_status_type` varchar(200) DEFAULT NULL,
  `f_subversion` varchar(200) DEFAULT NULL,
  `f_version` varchar(200) DEFAULT NULL,
  `f_cert_num` varchar(200) DEFAULT NULL,
  `f_reg_date` datetime DEFAULT NULL,
  `f_req_id` int(11) DEFAULT NULL,
  `f_status_code` int(11) DEFAULT NULL,
  `f_act_code` varchar(200) DEFAULT NULL,
  `f_error_code` varchar(200) DEFAULT NULL,
  `f_error_description` varchar(200) DEFAULT NULL,
  `f_lic_no` varchar(200) DEFAULT NULL,
  `f_originator_code` varchar(200) DEFAULT NULL,
  `f_originator_name` varchar(200) DEFAULT NULL,
  `f_recipient_code` varchar(200) DEFAULT NULL,
  `f_recipient_name` varchar(200) DEFAULT NULL,
  `f_sender_code` varchar(200) DEFAULT NULL,
  `f_sender_name` varchar(200) DEFAULT NULL,
  `f_bvuname` varchar(200) DEFAULT NULL,
  `f_document_date` datetime DEFAULT NULL,
  `f_document_file` longtext,
  `f_document_file_name` varchar(200) DEFAULT NULL,
  `f_document_file_size` double DEFAULT NULL,
  `f_document_name` varchar(200) DEFAULT NULL,
  `f_document_number` varchar(200) DEFAULT NULL,
  `f_full_name` varchar(200) DEFAULT NULL,
  `f_request_id` varchar(200) DEFAULT NULL,
  `f_res_document_date` datetime DEFAULT NULL,
  `f_res_document_name` varchar(200) DEFAULT NULL,
  `f_res_document_number` varchar(200) DEFAULT NULL,
  `f_status` int(11) DEFAULT NULL,
  `f_citizen__birthday` datetime DEFAULT NULL,
  `f_citizen__birthplace` varchar(200) DEFAULT NULL,
  `f_citizen__firstname` varchar(200) DEFAULT NULL,
  `f_citizen__givenname` varchar(200) DEFAULT NULL,
  `f_citizen__lastname` varchar(200) DEFAULT NULL,
  `f_citizen__snils` varchar(200) DEFAULT NULL,
  `f_comment` longtext,
  `f_region` varchar(200) DEFAULT NULL,
  `f_service_code` varchar(200) DEFAULT NULL,
  `f_task_id` varchar(200) DEFAULT NULL,
  `f_version_code` varchar(200) DEFAULT NULL,
  `f_debts_payer` varchar(200) DEFAULT NULL,
  `f_inn_payer` varchar(200) DEFAULT NULL,
  `f_kpp_payer` varchar(200) DEFAULT NULL,
  `f_name_payer` varchar(200) DEFAULT NULL,
  `f_f_name_civ` varchar(200) DEFAULT NULL,
  `f_i_name_civ` varchar(200) DEFAULT NULL,
  `f_m_name_civ` varchar(200) DEFAULT NULL,
  `f_code_kind_spr` varchar(200) DEFAULT NULL,
  `f_doc_dat_civ` datetime DEFAULT NULL,
  `f_series_number` varchar(200) DEFAULT NULL,
  `f_blank_no` varchar(200) DEFAULT NULL,
  `f_date_issue` datetime DEFAULT NULL,
  `f_document_no` varchar(200) DEFAULT NULL,
  `f_error_desc` varchar(200) DEFAULT NULL,
  `f_error_no` varchar(200) DEFAULT NULL,
  `f_part_firm_requisite` longtext,
  `f_part_product` longtext,
  `f_doc__country__link` varchar(200) DEFAULT NULL,
  `f_doc__id` varchar(200) DEFAULT NULL,
  `f_doc__issuedate` datetime DEFAULT NULL,
  `f_doc__type__link` varchar(200) DEFAULT NULL,
  `f_for__firstname` varchar(200) DEFAULT NULL,
  `f_for__givenname` varchar(200) DEFAULT NULL,
  `f_for__lastname` varchar(200) DEFAULT NULL,
  `f_reg__address` varchar(200) DEFAULT NULL,
  `f_reg__date` datetime DEFAULT NULL,
  `f_reg__type` varchar(200) DEFAULT NULL,
  `f_doc__number` varchar(200) DEFAULT NULL,
  `f_doc__serie` varchar(200) DEFAULT NULL,
  `f_doc__typelink` varchar(200) DEFAULT NULL,
  `f_lplace__building` varchar(200) DEFAULT NULL,
  `f_lplace__city` varchar(200) DEFAULT NULL,
  `f_lplace__district` varchar(200) DEFAULT NULL,
  `f_lplace__flat` varchar(200) DEFAULT NULL,
  `f_lplace__house` varchar(200) DEFAULT NULL,
  `f_reg__date__till` datetime DEFAULT NULL,
  `f_mchsterritorial_office_name` longtext,
  `f_not_found` tinyint(1) DEFAULT NULL,
  `f_not_found_description` longtext,
  `f_recipient_organization_full_name` longtext,
  `f_report_issue_date` datetime DEFAULT NULL,
  `f_report_validity_period` datetime DEFAULT NULL,
  PRIMARY KEY (`f_guid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;

-- Дамп данных таблицы offlinedb.t_smev_entity: ~0 rows (приблизительно)
/*!40000 ALTER TABLE `t_smev_entity` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_smev_entity` ENABLE KEYS */;


-- Дамп структуры для таблица offlinedb.users
CREATE TABLE IF NOT EXISTS `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` bigint(20) DEFAULT NULL,
  `account_expired` bit(1) DEFAULT NULL,
  `account_locked` bit(1) DEFAULT NULL,
  `enabled` bit(1) NOT NULL,
  `password` varchar(255) NOT NULL,
  `password_expired` bit(1) DEFAULT NULL,
  `username` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- Дамп данных таблицы offlinedb.users: ~2 rows (приблизительно)
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` (`id`, `version`, `account_expired`, `account_locked`, `enabled`, `password`, `password_expired`, `username`) VALUES
	(1, 0, b'0', b'0', b'1', '$2a$10$MU83UwFO79OgGzqo8/EbA.GzpcZlBvzVT.uPF9isKAOVH5cEM/Eui', b'0', 'admin');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;


-- Дамп структуры для таблица offlinedb.user_authority
CREATE TABLE IF NOT EXISTS `user_authority` (
  `authority_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`authority_id`,`user_id`),
  KEY `FKB55BEBCF6BB5679D` (`user_id`),
  KEY `FKB55BEBCF347C1AF7` (`authority_id`),
  CONSTRAINT `FKB55BEBCF347C1AF7` FOREIGN KEY (`authority_id`) REFERENCES `authority` (`id`),
  CONSTRAINT `FKB55BEBCF6BB5679D` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Дамп данных таблицы offlinedb.user_authority: ~2 rows (приблизительно)
/*!40000 ALTER TABLE `user_authority` DISABLE KEYS */;
INSERT INTO `user_authority` (`authority_id`, `user_id`) VALUES
	(2, 1);
/*!40000 ALTER TABLE `user_authority` ENABLE KEYS */;


-- Дамп структуры для таблица offlinedb.user_property
CREATE TABLE IF NOT EXISTS `user_property` (
  `name` varchar(255) NOT NULL,
  `value` varchar(255) DEFAULT NULL,
  `user_id` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`name`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Дамп данных таблицы offlinedb.user_property: ~0 rows (приблизительно)
/*!40000 ALTER TABLE `user_property` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_property` ENABLE KEYS */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
