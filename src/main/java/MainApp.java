import java.sql.*;
import java.time.LocalDate;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.Scanner;

public class MainApp {
    private static Scanner scanner = new Scanner(System.in);

    static void main(String[] args) {

        int opt;
        do {
            System.out.println("\n---Meniu---");
            System.out.println("1. Adauga persoana");
            System.out.println("2. Adauga excursie");
            System.out.println("3. Afiseaza persoane si excursii");
            System.out.println("4. Afiseaza excursile unei anumite persoane: ");
            System.out.println("5.Afișarea tuturor persoanelor care au vizitat o anumita destinație: ");
            System.out.println("6. Afiseaza persoanele care au facut excursii intr un anumit an: ");
            System.out.println("7. Ștergerea unei excursii");
            System.out.println("8. Ștergerea unei persoane (împreună cu excursiile în care a fost)");
            System.out.println("0. Iesire");
            System.out.print("Optiune: ");
            opt = Integer.parseInt(scanner.nextLine());

            switch (opt) {
                case 1:
                    adaugaPersoana();
                    break;
                case 2:
                    adaugaExcursie();
                    break;
                case 3:
                    afiseazaPersoaneExcursii();
                    break;
                case 4:
                    afiseazaExcursileUneiPersoane();
                    break;
                case 5:
                    afiseazaPersoaneDestinatie();
                    break;
                case 6:
                    afiseazaPersoaneAn();
                    break;
                case 7:
                    stergeExcrusie();
                    break;
                case 8:
                    stergePersoana();
                    break;
                case 0:
                    System.out.println("La revedere! ");
                    break;
                default:
                    System.out.println("Optiune invalida!");
            }

        } while (opt != 0);
    }

    private static void adaugaPersoana() {
        try (Connection conn = ConexiuneBD.getConnection()) {
            System.out.println("Nume: ");
            String nume = scanner.nextLine();

            System.out.println("Varsta: ");
            int varsta = Integer.parseInt(scanner.nextLine());
            if (varsta < 0 || varsta > 120) throw new ExceptieVarsta("Varsta invalida! ");

            String sql = "INSERT INTO persoane(nume,varsta) VALUES (?,?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, nume);
                ps.setInt(2, varsta);
                ps.executeUpdate();
                System.out.println("Persoana adaugata! ");
            }
        } catch (ExceptieVarsta e) {
            System.out.println("Eroare: " + e.getMessage());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static int getVarstaPersoana(int idPersoana, Connection conn) {
        String sql = "SELECT varsta FROM persoane WHERE id= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPersoana);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("varsta");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    public static void adaugaExcursie() {
        try (Connection conn = ConexiuneBD.getConnection()) {
            System.out.println("Id persoana: ");
            int idPersoana = Integer.parseInt(scanner.nextLine());
            String sqlCheck = "SELECT id FROM persoane WHERE id= ?";
            try (PreparedStatement psCheck = conn.prepareStatement(sqlCheck)) {
                psCheck.setInt(1, idPersoana);
                ResultSet rs = psCheck.executeQuery();
                if (!rs.next()) {
                    System.out.println("Persoana nu exista! ");
                    return;
                }
            }
            System.out.println("Destinatie: ");
            String destinatie = scanner.nextLine();
            System.out.println("An excursie: ");
            int anExcursie = Integer.parseInt(scanner.nextLine());

            int varstaPersoana = getVarstaPersoana(idPersoana, conn);

            int anNastere = LocalDate.now().getYear() - varstaPersoana;
            if (anNastere > LocalDate.now().getYear() || anExcursie > LocalDate.now().getYear())
                throw new ExceptieAnExcursie("An excursie invalid! ");

            String sql = "INSERT INTO excursii(id_persoana,destinatia,anul)  VALUES(?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idPersoana);
                ps.setString(2, destinatie);
                ps.setInt(3, anExcursie);
                ps.executeUpdate();
                System.out.println("Excursie adaugata!");
            }
        } catch (ExceptieAnExcursie e) {
            System.out.println("Eroare! " + e.getMessage());

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void afiseazaPersoaneExcursii() {
        try (Connection conn = ConexiuneBD.getConnection()) {
            String sql = "SELECT p.id, p.nume, p.varsta, e.id_excursie,e.destinatia, e.anul FROM persoane p LEFT JOIN excursii e ON p.id = e.id_persoana  ORDER BY p.id";
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    System.out.printf("ID: %d, Nume: %s, Varsta: %d, Id excursie: %d, Destinatie: %s, An: %d\n", rs.getInt("id"), rs.getString("nume"), rs.getInt("varsta"), rs.getInt("id_excursie"), rs.getString("destinatia"), rs.getInt("anul"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void afiseazaExcursileUneiPersoane() {
        System.out.print("Nume persoana: ");
        String nume = scanner.nextLine();
        try (Connection conn = ConexiuneBD.getConnection()) {
            String sql = "SELECT e.id_excursie, e.destinatia, e.anul FROM persoane p INNER JOIN excursii e ON p.id=e.id_persoana WHERE p.nume= ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, nume);
                ResultSet rs = ps.executeQuery();
                boolean gasit = false;
                while (rs.next()) {
                    System.out.printf("Id excursie: %d, Destinatie: %s, An: %d\n", rs.getInt("id_excursie"), rs.getString("destinatia"), rs.getInt("anul"));
                    gasit = true;
                }
                if (!gasit) System.out.println("Persoane nu a facut excursii/ nu exista! ");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void afiseazaPersoaneDestinatie() {
        System.out.print("Destinatie: ");
        String destinatie = scanner.nextLine();

        String sql = "SELECT DISTINCT p.id, p.nume,p.varsta FROM persoane p INNER JOIN excursii e ON p.id=e.id_persoana WHERE e.destinatia= ?";
        try (Connection conn = ConexiuneBD.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, destinatie);
                ResultSet rs = ps.executeQuery();
                boolean gasit = false;
                while (rs.next()) {
                    System.out.printf("Id pers %d, Nume: %s, Varsta: %d\n", rs.getInt("id"), rs.getString("nume"), rs.getInt("varsta"));
                    gasit = true;
                }
                if (!gasit)
                    System.out.println("Nicio pers. nu a facut excursii in destinatia introdusa: " + destinatie);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void afiseazaPersoaneAn() {
        System.out.print("Anul excursii: ");
        int an = Integer.parseInt(scanner.nextLine());

        String sql = "SELECT DISTINCT p.id, p.nume,p.varsta FROM persoane p INNER JOIN excursii e ON p.id=e.id_persoana WHERE e.anul= ?";
        try (Connection conn = ConexiuneBD.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, an);
                ResultSet rs = ps.executeQuery();
                boolean gasit = false;
                while (rs.next()) {
                    System.out.printf("Id pers %d, Nume: %s, Varsta: %d\n", rs.getInt("id"), rs.getString("nume"), rs.getInt("varsta"));
                    gasit = true;
                }
                if (!gasit) System.out.println("Nicio pers. nu a facut excursii in anul: " + an);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void stergeExcrusie() {
        System.out.println("ID excursie: ");
        int id = Integer.parseInt(scanner.nextLine());
        String sql = "DELETE FROM excursii WHERE id_excursie= ?";
        try (Connection conn = ConexiuneBD.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                int row = ps.executeUpdate();
                if (row > 0) System.out.println("Excursie stearsa! ");
                else System.out.println("Nu sunt excursii cu id ul: " + id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void stergePersoana() {
        System.out.println("Id persoanei: ");
        int id = Integer.parseInt(scanner.nextLine());

        try (Connection conn = ConexiuneBD.getConnection()) {
            conn.setAutoCommit(false);

            String sqlExcursii = "DELETE FROM excursii WHERE id_persoana= ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlExcursii)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }

            String sqlPersoana = "DELETE FROM persoane WHERE id= ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlPersoana)) {
                ps.setInt(1, id);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    conn.commit();
                    System.out.println("Persoana si excursiile ei au fost sterse! ");
                } else {
                    conn.rollback();
                    System.out.println("Nu exista persoana cu acest ID: " + id);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
