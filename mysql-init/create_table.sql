# database init script
# author: Eddy Zhang


-- create database if not exists `project_contest_platform` default character set utf8 collate utf8_general_ci;
create database if not exists `project_contest_platform` default character set utf8 collate utf8_general_ci;

-- use `project_contest_platform`;
USE project_contest_platform;

-- Create users table
CREATE TABLE users (
                       id VARCHAR(36) PRIMARY KEY COMMENT 'User ID (UUID)',
                       name VARCHAR(50) NOT NULL COMMENT 'User Name',
                       email VARCHAR(100) UNIQUE NOT NULL COMMENT 'Email (Unique)',
                       password VARCHAR(72) NOT NULL COMMENT 'Encrypted Password (bcrypt)',
                       description TEXT COMMENT 'User Description',
                       avatar_url VARCHAR(255) COMMENT 'Avatar URL',
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Created At',
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated At'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User Table';

-- Create roles table
CREATE TABLE roles (
                       id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'Role ID',
                       name ENUM('Admin', 'Organizer', 'Participant', 'Judge') UNIQUE NOT NULL COMMENT 'Role Name',
                       description TEXT COMMENT 'Role Description',
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Created At',
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated At'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Role Table';

-- Create user-roles mapping table
CREATE TABLE user_roles (
                            user_id VARCHAR(36) NOT NULL COMMENT 'User ID (UUID)',
                            role_id INT NOT NULL COMMENT 'Role ID (Auto-Increment)',
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Created At',
                            PRIMARY KEY (user_id, role_id), -- 联合主键，避免重复
                            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                            FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User-Role Mapping Table';


-- Insert roles data
INSERT INTO roles (name, description) VALUES
                                          ('Admin', 'System Administrator'),
                                          ('Organizer', 'Competition Organizer'),
                                          ('Participant', 'Competition Participant'),
                                          ('Judge', 'Competition Judge');

CREATE TABLE team (
                      id CHAR(36) PRIMARY KEY COMMENT 'Team ID (UUID)',
                      name VARCHAR(100) NOT NULL COMMENT 'Team Name',
                      description TEXT COMMENT 'Team Description',
                      created_by CHAR(36) NOT NULL COMMENT 'User ID of team creator',
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation Timestamp',
                      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last Updated',
                      FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Table for team information';

CREATE TABLE team_members (
                              id CHAR(36) PRIMARY KEY COMMENT 'Team Member Mapping ID (UUID)',
                              team_id CHAR(36) NOT NULL COMMENT 'Team ID',
                              user_id CHAR(36) NOT NULL COMMENT 'User ID',
                              role VARCHAR(50) DEFAULT 'MEMBER' COMMENT 'Role within the team (customizable)',
                              joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Join Timestamp',
                              UNIQUE KEY uq_team_user (team_id, user_id),
                              FOREIGN KEY (team_id) REFERENCES team(id) ON DELETE CASCADE,
                              FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                              INDEX idx_team (team_id),
                              INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Mapping table between users and teams';

-- Create competitions table
CREATE TABLE competitions (
                              id CHAR(36) PRIMARY KEY COMMENT 'Primary key: Competition ID (UUID)',
                              name VARCHAR(255) NOT NULL COMMENT 'Competition name',
                              description TEXT COMMENT 'Detailed description of the competition',
                              category VARCHAR(100) COMMENT 'Competition category (e.g., AI, Robotics, Design)',
                              start_date TIMESTAMP NOT NULL COMMENT 'Competition start time',
                              end_date TIMESTAMP NOT NULL COMMENT 'Competition end time',
                              is_public BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Is the competition visible to the public',
                              status VARCHAR(50) NOT NULL COMMENT 'Competition status',
                              participation_type ENUM('INDIVIDUAL', 'TEAM') NOT NULL DEFAULT 'INDIVIDUAL' COMMENT 'Participation mode: INDIVIDUAL or TEAM',
                              allowed_submission_types JSON COMMENT 'Allowed submission file types (e.g., ["PDF", "ZIP"])',
                              scoring_criteria JSON COMMENT 'Scoring criteria (e.g., [{"criterion": "Innovation", "weight": 0.4}])',
                              intro_video_url VARCHAR(512) COMMENT 'URL for competition introduction video',
                              image_urls JSON COMMENT 'JSON array of image URLs used for competition banners etc.',
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
                              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Record update timestamp'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Competitions table';

-- Create competition_organizers, competition_participants, competition_judges tables
CREATE TABLE competition_organizers (
                                        id             CHAR(36) PRIMARY KEY COMMENT 'Surrogate primary key (UUID)',
                                        competition_id CHAR(36) NOT NULL COMMENT 'Competition ID (UUID)',
                                        user_id        CHAR(36) NOT NULL COMMENT 'Organizer user ID (UUID)',
                                        created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation time',
                                        updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Record update time',
                                        UNIQUE KEY uq_competition_user (competition_id, user_id),
                                        INDEX idx_competition (competition_id),
                                        INDEX idx_user (user_id),
                                        FOREIGN KEY (competition_id) REFERENCES competitions(id) ON DELETE CASCADE,
                                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Table for competition organizers (many-to-many relationship)';

CREATE TABLE competition_participants (
                                          id             CHAR(36) PRIMARY KEY COMMENT 'Surrogate primary key (UUID)',
                                          competition_id CHAR(36) NOT NULL COMMENT 'Competition ID (UUID)',
                                          user_id        CHAR(36) NOT NULL COMMENT 'Participant user ID (UUID)',
                                          created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation time',
                                          updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Record update time',
                                          UNIQUE KEY uq_competition_user (competition_id, user_id),
                                          INDEX idx_competition (competition_id),
                                          INDEX idx_user (user_id),
                                          FOREIGN KEY (competition_id) REFERENCES competitions(id) ON DELETE CASCADE,
                                          FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Table for competition participants (many-to-many relationship)';

CREATE TABLE competition_teams (
                                   id CHAR(36) PRIMARY KEY COMMENT 'Mapping ID (UUID)',
                                   competition_id CHAR(36) NOT NULL COMMENT 'Competition ID',
                                   team_id CHAR(36) NOT NULL COMMENT 'Team ID',
                                   joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Join Time',
                                   UNIQUE KEY uq_competition_team (competition_id, team_id),
                                   FOREIGN KEY (competition_id) REFERENCES competitions(id) ON DELETE CASCADE,
                                   FOREIGN KEY (team_id) REFERENCES team(id) ON DELETE CASCADE,
                                   INDEX idx_competition (competition_id),
                                   INDEX idx_team (team_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Mapping between teams and competitions';

CREATE TABLE competition_judges (
                                    id             CHAR(36) PRIMARY KEY COMMENT 'Surrogate primary key (UUID)',
                                    competition_id CHAR(36) NOT NULL COMMENT 'Competition ID (UUID)',
                                    user_id        CHAR(36) NOT NULL COMMENT 'Judge user ID (UUID)',
                                    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation time',
                                    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Record update time',
                                    UNIQUE KEY uq_competition_user (competition_id, user_id),
                                    INDEX idx_competition (competition_id),
                                    INDEX idx_user (user_id),
                                    FOREIGN KEY (competition_id) REFERENCES competitions(id) ON DELETE CASCADE,
                                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Table for competition judges (many-to-many relationship)';

CREATE TABLE submission_records (
                                    id CHAR(36) PRIMARY KEY COMMENT 'Submission record ID (UUID)',
                                    competition_id CHAR(36) NOT NULL COMMENT 'ID of the competition',
                                    user_id CHAR(36) DEFAULT NULL COMMENT 'ID of the participant (for individual submissions)',
                                    team_id CHAR(36) DEFAULT NULL COMMENT 'ID of the team (for team submissions)',
                                    title VARCHAR(255) NOT NULL COMMENT 'Title of the submission',
                                    description TEXT COMMENT 'Optional description of the submission',
                                    file_name VARCHAR(255) COMMENT 'Original file name of the uploaded submission',
                                    file_url VARCHAR(512) NOT NULL COMMENT 'URL to access the uploaded file (e.g., MinIO)',
                                    file_type VARCHAR(50) COMMENT 'File type (e.g., PDF, ZIP, DOCX)',
                                    review_status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING' COMMENT 'Status of review',
                                    review_comments TEXT COMMENT 'Comments from the reviewer during review',
                                    reviewed_by CHAR(36) DEFAULT NULL COMMENT 'User ID of the reviewer (organizer/admin)',
                                    reviewed_at TIMESTAMP NULL COMMENT 'Time when review was completed',
                                    total_score DECIMAL(5,2) DEFAULT NULL COMMENT 'Final total score given by reviewer (optional)',
                                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
                                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Record update timestamp',
                                    UNIQUE KEY uq_competition_user (competition_id, user_id),
                                    UNIQUE KEY uq_competition_team (competition_id, team_id),
                                    INDEX idx_competition (competition_id),
                                    INDEX idx_user (user_id),
                                    INDEX idx_team (team_id),
                                    FOREIGN KEY (competition_id) REFERENCES competitions(id) ON DELETE CASCADE,
                                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
                                    FOREIGN KEY (team_id) REFERENCES team(id) ON DELETE SET NULL,
                                    FOREIGN KEY (reviewed_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Submission records for both individual and team competition submissions';

CREATE TABLE submission_comments (
                                     id CHAR(36) PRIMARY KEY COMMENT 'Comment ID (UUID)',
                                     submission_id CHAR(36) NOT NULL COMMENT 'ID of the submission being commented on',
                                     user_id CHAR(36) NOT NULL COMMENT 'User ID of the commenter',
                                     parent_id CHAR(36) DEFAULT NULL COMMENT 'Parent comment ID (null if top-level comment)',
                                     content TEXT NOT NULL COMMENT 'Comment content',
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Comment timestamp',
                                     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last updated',

                                     FOREIGN KEY (submission_id) REFERENCES submission_records(id) ON DELETE CASCADE,
                                     FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                                     FOREIGN KEY (parent_id) REFERENCES submission_comments(id) ON DELETE CASCADE,

                                     INDEX idx_submission (submission_id),
                                     INDEX idx_user (user_id),
                                     INDEX idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Comments with optional parent for nested replies';

CREATE TABLE submission_votes (
                                  id CHAR(36) PRIMARY KEY COMMENT 'Vote ID (UUID)',
                                  submission_id CHAR(36) NOT NULL COMMENT 'ID of the voted submission',
                                  user_id CHAR(36) NOT NULL COMMENT 'User ID who voted',
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Vote timestamp',
                                  FOREIGN KEY (submission_id) REFERENCES submission_records(id) ON DELETE CASCADE,
                                  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                                  UNIQUE KEY uq_submission_user (submission_id, user_id),
                                  INDEX idx_submission (submission_id),
                                  INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Votes on submissions';

CREATE TABLE submission_judges (
                                   id CHAR(36) PRIMARY KEY COMMENT 'Judge Record ID (UUID)',
                                   submission_id CHAR(36) NOT NULL COMMENT 'ID of the submission being judged',
                                   competition_id CHAR(36) NOT NULL COMMENT 'Competition ID',
                                   judge_id CHAR(36) NOT NULL COMMENT 'User ID of the judge',
                                   total_score DECIMAL(5,2) DEFAULT NULL COMMENT 'Final total score given by the judge',
                                   judge_comments TEXT COMMENT 'General comments from the judge',
                                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Judge record creation timestamp',
                                   updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last updated timestamp',

                                   FOREIGN KEY (submission_id) REFERENCES submission_records(id) ON DELETE CASCADE,
                                   FOREIGN KEY (competition_id) REFERENCES competitions(id) ON DELETE CASCADE,
                                   FOREIGN KEY (judge_id) REFERENCES users(id) ON DELETE CASCADE,

                                   UNIQUE KEY uq_submission_judge (submission_id, judge_id),
                                   INDEX idx_submission (submission_id),
                                   INDEX idx_judge (judge_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Judge records for submissions';

CREATE TABLE submission_judge_scores (
                                         id CHAR(36) PRIMARY KEY COMMENT 'Judge Score ID (UUID)',
                                         judge_record_id CHAR(36) NOT NULL COMMENT 'Associated judge record ID (links to submission_judges)',
                                         submission_id CHAR(36) NOT NULL COMMENT 'ID of the submission (foreign key to submission_records)',
                                         criterion VARCHAR(100) NOT NULL COMMENT 'Scoring criterion (e.g., Creativity)',
                                         score DECIMAL(5,2) NOT NULL COMMENT 'Score assigned for this criterion',
                                         weight DECIMAL(5,2) NOT NULL COMMENT 'Weight of this criterion (copied from competition settings)',

                                         FOREIGN KEY (judge_record_id) REFERENCES submission_judges(id) ON DELETE CASCADE,
                                         FOREIGN KEY (submission_id) REFERENCES submission_records(id) ON DELETE CASCADE ON UPDATE CASCADE,

                                         INDEX idx_judge_record (judge_record_id),
                                         INDEX idx_submission_id (submission_id),
                                         INDEX idx_criterion (criterion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Individual criterion scores assigned by judges';

CREATE TABLE submission_winners (
                                    id CHAR(36) PRIMARY KEY COMMENT 'Primary Key (UUID)',
                                    competition_id CHAR(36) NOT NULL COMMENT 'ID of the competition',
                                    submission_id CHAR(36) NOT NULL COMMENT 'ID of the submission (work)',
                                    award_name VARCHAR(255) NOT NULL COMMENT 'Award name (e.g., Champion, Best Design)',
                                    rank_submission INT DEFAULT NULL COMMENT 'Submission ranking (1 = 1st place, 2 = 2nd place, etc.)',
                                    award_description TEXT DEFAULT NULL COMMENT 'Optional detailed description about the award',
                                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
                                    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Record update timestamp',
                                    UNIQUE KEY uq_submission_award (competition_id, submission_id, award_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Table for recording awarded submissions';

-- Insert default admin user
INSERT INTO users (id, name, email, password, description, avatar_url)
VALUES (
           'e23f0b51-8de4-4cf3-9257-a0aa32bf3ece',
           'Admin',
           'admin@gmail.com',
           '$2a$10$gL.cMeF37Xty.1K7RSX.NOIGmwV5xoN9NPg6eR6dtcygVDXQW9bfS',
           'Default system administrator',
           NULL
       );

-- Bind Admin role to the default user
INSERT INTO user_roles (user_id, role_id)
VALUES (
           'e23f0b51-8de4-4cf3-9257-a0aa32bf3ece',
           (SELECT id FROM roles WHERE name = 'Admin')
       );
