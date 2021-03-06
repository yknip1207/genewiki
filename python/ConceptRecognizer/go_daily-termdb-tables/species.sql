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
-- Table structure for table `species`
--

DROP TABLE IF EXISTS `species`;
CREATE TABLE `species` (
  `id` int(11) NOT NULL auto_increment,
  `ncbi_taxa_id` int(11) default NULL,
  `common_name` varchar(255) default NULL,
  `lineage_string` text,
  `genus` varchar(55) default NULL,
  `species` varchar(255) default NULL,
  `parent_id` int(11) default NULL,
  `left_value` int(11) default NULL,
  `right_value` int(11) default NULL,
  `taxonomic_rank` varchar(255) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `sp0` (`id`),
  UNIQUE KEY `ncbi_taxa_id` (`ncbi_taxa_id`),
  KEY `sp1` (`ncbi_taxa_id`),
  KEY `sp2` (`common_name`),
  KEY `sp3` (`genus`),
  KEY `sp4` (`species`),
  KEY `sp5` (`genus`,`species`),
  KEY `sp6` (`id`,`ncbi_taxa_id`),
  KEY `sp7` (`id`,`ncbi_taxa_id`,`genus`,`species`),
  KEY `sp8` (`parent_id`),
  KEY `sp9` (`left_value`),
  KEY `sp10` (`right_value`),
  KEY `sp11` (`left_value`,`right_value`),
  KEY `sp12` (`id`,`left_value`),
  KEY `sp13` (`genus`,`left_value`,`right_value`)
) TYPE=MyISAM;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2010-07-22  8:43:20
