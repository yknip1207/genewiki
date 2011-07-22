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
-- Table structure for table `relation_composition`
--

DROP TABLE IF EXISTS `relation_composition`;
CREATE TABLE `relation_composition` (
  `id` int(11) NOT NULL auto_increment,
  `relation1_id` int(11) NOT NULL,
  `relation2_id` int(11) NOT NULL,
  `inferred_relation_id` int(11) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `relation1_id` (`relation1_id`,`relation2_id`,`inferred_relation_id`),
  KEY `rc1` (`relation1_id`),
  KEY `rc2` (`relation2_id`),
  KEY `rc3` (`inferred_relation_id`),
  KEY `rc4` (`relation1_id`,`relation2_id`,`inferred_relation_id`)
) TYPE=MyISAM AUTO_INCREMENT=21;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2010-07-22  8:43:20
