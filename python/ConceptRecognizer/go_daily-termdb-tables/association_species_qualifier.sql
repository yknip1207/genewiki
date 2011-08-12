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
-- Table structure for table `association_species_qualifier`
--

DROP TABLE IF EXISTS `association_species_qualifier`;
CREATE TABLE `association_species_qualifier` (
  `id` int(11) NOT NULL auto_increment,
  `association_id` int(11) NOT NULL,
  `species_id` int(11) default NULL,
  PRIMARY KEY  (`id`),
  KEY `association_id` (`association_id`),
  KEY `species_id` (`species_id`)
) TYPE=MyISAM;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2010-07-22  8:43:19
