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
-- Table structure for table `term`
--

DROP TABLE IF EXISTS `term`;
CREATE TABLE `term` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL default '',
  `term_type` varchar(55) NOT NULL,
  `acc` varchar(255) NOT NULL,
  `is_obsolete` int(11) NOT NULL default '0',
  `is_root` int(11) NOT NULL default '0',
  `is_relation` int(11) NOT NULL default '0',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `acc` (`acc`),
  UNIQUE KEY `t0` (`id`),
  KEY `t1` (`name`),
  KEY `t2` (`term_type`),
  KEY `t3` (`acc`),
  KEY `t4` (`id`,`acc`),
  KEY `t5` (`id`,`name`),
  KEY `t6` (`id`,`term_type`),
  KEY `t7` (`id`,`acc`,`name`,`term_type`)
) TYPE=MyISAM AUTO_INCREMENT=32091;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2010-07-22  8:43:20
