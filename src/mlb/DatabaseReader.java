package mlb;
/**
 * @author Roman Yasinovskyy
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseReader {
    private Connection db_connection;
    private final String SQLITEDBPATH = "jdbc:sqlite:data/mlb.sqlite";
    
    public DatabaseReader() { }
    /**
     * Connect to a database (file)
     */
    public void connect() {
        try {
            this.db_connection = DriverManager.getConnection(SQLITEDBPATH);
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseReaderGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Disconnect from a database (file)
     */
    public void disconnect() {
        try {
            this.db_connection.close();
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseReaderGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Populate the list of divisions
     * @param divisions
     */
    public void getDivisions(ArrayList<String> divisions) {
        Statement stat;
        ResultSet results;
        
        this.connect();
        try {
            // TODO: Write an SQL statement to retrieve a league (conference) and a division
            //National | East ... National | West ... National | Central ... American | East ... 6 combos in upper left
            //Team table has all conference and divisions
            //Select team_conference, team_division from teams; //make sure it's distinct 
            //divisions.add(results.getString(1) + "|" + results.getString(2)); 
            stat = this.db_connection.createStatement();
            results = stat.executeQuery("SELECT conference, division FROM team GROUP BY conference, division;");
            while (results.next()) {
                divisions.add(results.getString(1) + " | " + results.getString(2)); 
            } 
            
            // TODO: Add all 6 combinations to the ArrayList divisions
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseReader.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            this.disconnect();
        }
    }
    /**
     * Read all teams from the database
     * @param confDiv
     * @param teams
     */
    public void getTeams(String confDiv, ArrayList<String> teams) {
        Statement stat;
        ResultSet results;
        String conference = confDiv.split(" | ")[0];
        String division = confDiv.split(" | ")[2];
        
        this.connect();
        try {
            // TODO: Write an SQL statement to retrieve a teams from a specific division
            stat = this.db_connection.createStatement();
            results = stat.executeQuery("SELECT name FROM team WHERE team.division= '" + division + "' AND team.conference = '" + conference + "';");
            
            // TODO: Add all 5 teams to the ArrayList teams
            while (results.next()) {
                System.out.println(results.getString(1));
                teams.add(results.getString("name")); 
            } 
            results.close();
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseReader.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            this.disconnect();
        }
    }
    /**
     * @param teamName
     * @return Team info
     */
    public Team getTeamInfo(String teamName) {
        Team team = null;
        this.connect(); 
        try {
            // TODO: Retrieve team info (roster, address, and logo) from the database
            Statement statement = this.db_connection.createStatement();
            ResultSet teamResult = statement.executeQuery("SELECT * FROM team WHERE team.name= '" + teamName + "';");
            int team_id = teamResult.getInt("idpk"); 
            String id = teamResult.getString("id"); 
            String abbr = teamResult.getString("abbr"); 
            String name = teamResult.getString("name"); 
            String conf = teamResult.getString("conference"); 
            String div = teamResult.getString("division"); 
            team = new Team(id, abbr, name, conf, div);
            
            //Get Logo 
            byte[] theLogo = teamResult.getBytes("logo"); 
            team.setLogo(theLogo);
            
            //Get Roster
            ResultSet playerResult = statement.executeQuery("SELECT * FROM player WHERE player.team=" + team_id + ";"); 
            ArrayList<Player> roster = new ArrayList<>(); 
            while (playerResult.next()) {
                String playerId = playerResult.getString("id"); 
                String playerName = playerResult.getString("name"); 
                String position = playerResult.getString("position"); 
                String playerTeamName = playerResult.getString("team"); 
                Player player = new Player(playerId, playerName, playerTeamName, position); 
                
                roster.add(player); 
            }
            team.setRoster(roster);
            
            //Get Address
            ResultSet addressResult = statement.executeQuery("SELECT * FROM address WHERE address.team =" + team_id + ";"); 
            String site = addressResult.getString("site"); 
            String street = addressResult.getString("street");
            String city = addressResult.getString("city"); 
            String state = addressResult.getString("state"); 
            String zip = addressResult.getString("zip"); 
            String phone = addressResult.getString("phone"); 
            String url = addressResult.getString("url");
            String aTeamId = Integer.toString(team_id); 
            
            Address address = new Address(aTeamId, site, street, city, state, zip, phone, url); 
            team.setAddress(address);
            
            
        }
        catch (Exception e) {
            System.out.print(e.getMessage());
        }
        return team; 
    }
}
