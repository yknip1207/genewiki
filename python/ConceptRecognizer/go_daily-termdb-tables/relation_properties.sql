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
-- Table structure for table `relation_properties`
--

DROP TABLE IF EXISTS `relation_properties`;
CREATE TABLE `relation_properties` (
  `relationship_type_id` int(11) NOT NULL,
  `is_transitive` int(11) default NULL,
  `is_symmetric` int(11) default NULL,
  `is_anti_symmetric` int(11) default NULL,
  `is_cyclic` int(11) default NULL,
  `is_reflexive` int(11) default NULL,
  `is_metadata_tag` int(11) default NULL,
  UNIQUE KEY `relationship_type_id` (`relationship_type_id`),
  UNIQUE KEY `rp1` (`relationship_type_id`)
) TYPE=MyISAM;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2010-07-22  8:43:20
