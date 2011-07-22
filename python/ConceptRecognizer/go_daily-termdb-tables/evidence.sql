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
-- Table structure for table `evidence`
--

DROP TABLE IF EXISTS `evidence`;
CREATE TABLE `evidence` (
  `id` int(11) NOT NULL auto_increment,
  `code` varchar(8) NOT NULL,
  `association_id` int(11) NOT NULL,
  `dbxref_id` int(11) NOT NULL,
  `seq_acc` varchar(255) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `association_id` (`association_id`,`dbxref_id`,`code`),
  UNIQUE KEY `ev0` (`id`),
  UNIQUE KEY `ev5` (`id`,`association_id`),
  UNIQUE KEY `ev6` (`id`,`code`,`association_id`),
  KEY `ev1` (`association_id`),
  KEY `ev2` (`code`),
  KEY `ev3` (`dbxref_id`),
  KEY `ev4` (`association_id`,`code`)
) TYPE=MyISAM;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2010-07-22  8:43:19
