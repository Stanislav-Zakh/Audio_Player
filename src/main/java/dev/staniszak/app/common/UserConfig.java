package dev.staniszak.app.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserConfig {

        private String user_star_directory;
        private String user_lastplayed_track;

        public UserConfig() { }
 
        public UserConfig(String user_star_directory, String user_lastplayed_track) {
            this.user_star_directory = user_star_directory;
            this.user_lastplayed_track = user_lastplayed_track;
        }

}
