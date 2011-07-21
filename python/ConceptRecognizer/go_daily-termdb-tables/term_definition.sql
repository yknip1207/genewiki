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
-- Table structure for table `term_definition`
--

DROP TABLE IF EXISTS `term_definition`;
CREATE TABLE `term_definition` (
  `term_id` int(11) NOT NULL,
  `term_definition` text NOT NULL,
  `dbxref_id` int(11) default NULL,
  `term_comment` mediumtext,
  `reference` varchar(255) default NULL,
  UNIQUE KEY `term_id` (`term_id`),
  KEY `dbxref_id` (`dbxref_id`),
  KEY `td1` (`term_id`)
) TYPE=MyISAM;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2010-07-22  8:43:20
