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
-- Table structure for table `term2term_metadata`
--

DROP TABLE IF EXISTS `term2term_metadata`;
CREATE TABLE `term2term_metadata` (
  `id` int(11) NOT NULL auto_increment,
  `relationship_type_id` int(11) NOT NULL,
  `term1_id` int(11) NOT NULL,
  `term2_id` int(11) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `term1_id` (`term1_id`,`term2_id`),
  KEY `relationship_type_id` (`relationship_type_id`),
  KEY `term2_id` (`term2_id`)
) TYPE=MyISAM AUTO_INCREMENT=1851;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2010-07-22  8:43:20
