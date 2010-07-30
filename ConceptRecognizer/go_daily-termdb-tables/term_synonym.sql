-- MySQL dump 10.11
--
-- Host: localhost    Database: go
-- ------------------------------------------------------
-- Server version	5.0.45
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE=',MYSQL40' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `term_synonym`
--

DROP TABLE IF EXISTS `term_synonym`;
CREATE TABLE `term_synonym` (
  `term_id` int(11) NOT NULL,
  `term_synonym` varchar(996) default NULL,
  `acc_synonym` varchar(255) default NULL,
  `synonym_type_id` int(11) NOT NULL,
  `synonym_category_id` int(11) default NULL,
  UNIQUE KEY `term_id` (`term_id`,`term_synonym`),
  KEY `synonym_type_id` (`synonym_type_id`),
  KEY `synonym_category_id` (`synonym_category_id`),
  KEY `ts1` (`term_id`),
  KEY `ts2` (`term_synonym`),
  KEY `ts3` (`term_id`,`term_synonym`)
) TYPE=MyISAM;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2010-07-22  8:43:20
