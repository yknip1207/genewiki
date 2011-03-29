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
-- Table structure for table `graph_path`
--

DROP TABLE IF EXISTS `graph_path`;
CREATE TABLE `graph_path` (
  `id` int(11) NOT NULL auto_increment,
  `term1_id` int(11) NOT NULL,
  `term2_id` int(11) NOT NULL,
  `relationship_type_id` int(11) default NULL,
  `distance` int(11) default NULL,
  `relation_distance` int(11) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `graph_path0` (`id`),
  KEY `relationship_type_id` (`relationship_type_id`),
  KEY `graph_path1` (`term1_id`),
  KEY `graph_path2` (`term2_id`),
  KEY `graph_path3` (`term1_id`,`term2_id`),
  KEY `graph_path4` (`term1_id`,`distance`),
  KEY `graph_path5` (`term1_id`,`term2_id`,`relationship_type_id`),
  KEY `graph_path6` (`term1_id`,`term2_id`,`relationship_type_id`,`distance`,`relation_distance`),
  KEY `graph_path7` (`term2_id`,`relationship_type_id`),
  KEY `graph_path8` (`term1_id`,`relationship_type_id`)
) TYPE=MyISAM AUTO_INCREMENT=650702;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2010-07-22  8:43:19
