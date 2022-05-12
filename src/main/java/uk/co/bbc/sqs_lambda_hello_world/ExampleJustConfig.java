package uk.co.bbc.sqs_lambda_hello_world;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/*
** An example of an object loaded from just-configuration.
** This is a map of other things, just for kicks.
**
**  matches [
**      match1: {home: "russia", away: "saudi arabia"},
**      match2: {home: "egypt", away: "uruguay"},
**      match3: {home: "morocco", away: "iran"},
**      match4: {home: "portugal", away: "spain"}
**  ]
*/

@XmlRootElement
public class ExampleJustConfig {

    @JsonProperty("matches")
    private Map<String, Match> matches;

    public ExampleJustConfig() {
    }

    public ExampleJustConfig(final Map<String, Match> matches) {
        this.matches = matches;
    }

    public Map<String, Match> getMatches() {
        return matches;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(matches);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof ExampleJustConfig)) return false;
        final ExampleJustConfig that = (ExampleJustConfig) obj;
        return Objects.equals(matches, that.matches);
    }

    @Override
    public String toString() {
        return "ExampleJustConfig [matches=" + matches + "]";
    }

    public void addMatch(String name, String home, String away) {
        if (matches == null) {
            matches = new HashMap<>();
        }
        matches.put(name, new Match(home, away));
    }

    @XmlRootElement
    protected static class Match {
        @XmlElement
        String home;

        @XmlElement
        String away;

        public Match() {
        }

        public Match(String home, String away) {
            this.home = home;
            this.away = away;
        }

        public String getHome() {
            return home;
        }

        public String getAway() {
            return away;
        }

        @Override
        public int hashCode() {
            return Objects.hash(away, home);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Match)) return false;
            Match other = (Match) obj;
            return Objects.equals(away, other.away) && Objects.equals(home, other.home);
        }

        @Override
        public String toString() {
            return "Match [home=" + home + ", away=" + away + "]";
        }
    }
}
