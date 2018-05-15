--
-- Table
--

CREATE TABLE IF NOT EXISTS `test` (
  `age` tinyint(3) unsigned NOT NULL,
  `gender` enum('female','male') NOT NULL,
  `zipcode` varchar(5) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Data
--

INSERT INTO `test` (`age`, `gender`, `zipcode`) VALUES
(34, 'male', '81667'),
(45, 'female', '81675'),
(66, 'male', '81925'),
(70, 'female', '81931'),
(34, 'female', '81931'),
(70, 'male', '81931'),
(45, 'male', '81931');
