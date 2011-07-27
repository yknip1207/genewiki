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
-- Table structure for table `association`
--

DROP TABLE IF EXISTS `association`;
CREATE TABLE `association` (
  `id` int(11) NOT NULL auto_increment,
  `term_id` int(11) NOT NULL,
  `gene_product_id` int(11) NOT NULL,
  `is_not` int(11) default NULL,
  `role_group` int(11) default NULL,
  `assocdate` int(11) default NULL,
  `source_db_id` int(11) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `a0` (`id`),
  KEY `source_db_id` (`source_db_id`),
  KEY `a1` (`term_id`),
  KEY `a2` (`gene_product_id`),
  KEY `a3` (`term_id`,`gene_product_id`),
  KEY `a4` (`id`,`term_id`,`gene_product_id`),
  KEY `a5` (`id`,`gene_product_id`),
  KEY `a6` (`is_not`,`term_id`,`gene_product_id`),
  KEY `a7` (`assocdate`)
) TYPE=MyISAM;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2010-07-22  8:43:19
