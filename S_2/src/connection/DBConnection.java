package connection;

import gui.UserMods;

import java.sql.*;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

public class DBConnection{
    private final Connection conn;

    public Connection getConn() {
        return conn;
    }

    public DBConnection(String url, Properties props) throws SQLException {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        TimeZone timeZone = TimeZone.getTimeZone("GMT+7");
        TimeZone.setDefault(timeZone);
        Locale.setDefault(Locale.ENGLISH);

        conn = DriverManager.getConnection(url, props);
        conn.setAutoCommit(false);
    }

    public void close() throws SQLException {
        System.out.println("Delete tmp data...");
        Statement statement = conn.createStatement();

        //delete roles
        statement.executeUpdate("drop role c##admin");
        statement.executeUpdate("drop role c##reader");
        statement.executeUpdate("drop role c##librarian");

        //category
        statement.executeUpdate("drop table Teachers");
        statement.executeUpdate("drop table Researchers");
        statement.executeUpdate("drop table SchoolChild");
        statement.executeUpdate("drop table Students");
        statement.executeUpdate("drop table Pensioners");
        statement.executeUpdate("drop table Workers");
        statement.executeUpdate("drop table Others");
        //second level
        statement.executeUpdate("drop table Orders");
        statement.executeUpdate("drop sequence issued_seq");
        statement.executeUpdate("drop table Issued_books");
        statement.executeUpdate("drop sequence rules_seq");
        statement.executeUpdate("drop table Rules");
        statement.executeUpdate("drop sequence comp_seq");
        statement.executeUpdate("drop table Compositions");
        //Delete dependent tables 1 level
        statement.executeUpdate("drop table Librarians");
        statement.executeUpdate("drop table Readers");
        statement.executeUpdate("drop sequence edit_seq");
        statement.executeUpdate("drop table Editions");
        statement.executeUpdate("drop sequence USER_SEQ");
        statement.executeUpdate("drop table USERS");
        statement.executeUpdate("drop table HALLS");
        //Delete independent tables
        statement.executeUpdate("drop sequence lib_seq");
        statement.executeUpdate("drop table Libraries");
        System.out.println("Close connection...");
        conn.close();
    }

    public void setRole(UserMods userMod) throws SQLException {
        Statement statement = conn.createStatement();
        switch (userMod){
            case READER:{
                statement.executeUpdate("set role c##reader");
                break;
            }
            case LIBRARIAN:{
                statement.executeUpdate("set role c##librarian");
                break;
            }
            case ADMINISTRATOR:{
                statement.executeUpdate("set role c##admin");
                break;
            }
        }
    }

    public void initSchema() throws SQLException{
        System.out.println("Init schema....");

        try {
            Statement statement = conn.createStatement();
            //zero level
            statement.executeUpdate(
                    "create table Libraries(id_library integer primary key," +
                            " quantity_books integer not null," +
                            "name varchar(100) not null, " +
                            "check ( quantity_books >= 0 ))"
            );
            statement.executeUpdate("create sequence lib_seq start with 1 increment by 1 nomaxvalue");
            statement.executeUpdate("create trigger lib_trigger " +
                    "before insert on LIBRARIES " +
                    "referencing new as new_lib " +
                    "for each row " +
                    "begin " +
                    "if(:new_lib.id_library is null) then " +
                    "select lib_seq.nextval into :new_lib.id_library from DUAL; " +
                    "end if; " +
                    "end;");
            //first level
            statement.executeUpdate(
                    "create table Halls(" +
                            "id_hall integer," +
                            "check ( id_hall >= 0 ), " +
                            "id_library integer," +
                            "primary key (id_hall, id_library)," +
                            "foreign key (id_library) references LIBRARIES(ID_LIBRARY) on delete cascade)"
            );

            statement.executeUpdate(
                    "create table Users(" +
                            "user_id integer," +
                            "password varchar2(40) unique ," +
                            "user_mod varchar2(100)," +
                            "check ( user_mod = 'Администратор' or user_mod = 'Библиотекарь' or user_mod = 'Читатель')," +
                            "primary key (user_id)" +
                            ")");

            statement.executeUpdate("create sequence user_seq start with 1 increment by 1 nomaxvalue");
            statement.executeUpdate("create trigger user_trigger " +
                    "before insert on USERS " +
                    "referencing new as new_user " +
                    "for each row " +
                    "begin " +
                    "if(:new_user.user_id is null) then " +
                    "select user_seq.nextval into :new_user.user_id from DUAL; " +
                    "end if; " +
                    "end;");

            statement.executeUpdate(
                    "create table Librarians (" +
                            "id_librarian integer primary key, " +
                            "id_library integer not null , " +
                            "hall_num integer not null," +
                            "foreign key (id_librarian) references USERS(USER_ID) on delete cascade ," +
                            "foreign key (id_library, hall_num) references HALLS(ID_LIBRARY, ID_HALL) on delete cascade," +
                            " check ( hall_num > 0 ))"
            );

            statement.executeUpdate("" +
                    "create table Readers (" +
                    "id_reader integer primary key, " +
                    "id_library integer not null," +
                    "surname varchar(50) not null," +
                    "name varchar(40) not null," +
                    "patronymic varchar(60) not null," +
                    "status varchar(50) not null," +
                    "foreign key (id_reader) references USERS(USER_ID) on delete cascade ," +
                    "foreign key (id_library) references Libraries(id_library) on delete cascade " +
                    ")"
            );

            statement.executeUpdate(
                    "create table Editions(" +
                            "id_edition integer primary key ," +
                            "id_library integer not null," +
                            "hall_num integer not null," +
                            "rack_num integer not null," +
                            "shelf_num integer not null," +
                            "date_of_admission date not null," +
                            "write_off_date date not null," +
                            "check ( date_of_admission < write_off_date )," +
                            "check ( rack_num > 0 )," +
                            "check ( shelf_num > 0 )," +
                            "foreign key (id_library, hall_num) references HALLS(ID_LIBRARY, ID_HALL) on delete cascade " +
                            ")"
            );
            statement.executeUpdate("create sequence edit_seq start with 1 increment by 1 nomaxvalue ");
            statement.executeUpdate("create trigger edit_trigger " +
                    "before insert on EDITIONS " +
                    "referencing new as new_edit " +
                    "for each row " +
                    "begin " +
                    "if(:new_edit.id_edition is null) then " +
                    "select edit_seq.nextval into :new_edit.id_edition from DUAL;" +
                    "end if;" +
                    "end;");
            //second level
            statement.executeUpdate("create table Rules(" +
                    "id_rule integer primary key," +
                    " id_edition integer not null," +
                    " rule_text varchar(500) not null," +
                    " foreign key (id_edition) references Editions(id_edition) on delete cascade " +
                    ")"
            );
            statement.executeUpdate("create sequence rules_seq start with 1 increment by 1");
            statement.executeUpdate("create trigger rule_trigger " +
                    "before insert on RULES " +
                    "referencing new as new_rule " +
                    "for each row " +
                    "begin " +
                    "if(:new_rule.id_rule is null ) then " +
                    "select rules_seq.nextval into :new_rule.id_rule from DUAL;" +
                    "end if;" +
                    "end;");
            statement.executeUpdate("create table Compositions(" +
                    " id_record integer," +
                    " id_edition integer," +
                    " author varchar(100) not null," +
                    " title varchar(100) not null," +
                    " popularity NUMBER not null," +
                    "check ( popularity >= 0 and popularity <= 1 )," +
                    " genre varchar(50) not null," +
                    " primary key(id_record, id_edition)," +
                    " foreign key (id_edition) references Editions(id_edition) on delete cascade " +
                    ")"
            );


            statement.executeUpdate("create table Orders(" +
                    "id_reader integer," +
                    "id_edition integer," +
                    "id_librarian integer," +
                    "id_composition integer," +
                    "is_performed varchar(6) not null," +
                    "date_order date not null ," +
                    "check ( is_performed = 'да' or is_performed = 'нет' )," +
                    "primary key (id_reader, id_edition, id_composition)," +
                    "foreign key (id_reader) references Readers(ID_READER) on delete cascade ," +
                    "foreign key (id_edition, id_composition) references Compositions(ID_EDITION, ID_RECORD) on delete cascade ," +
                    "foreign key (id_librarian) references Librarians(ID_LIBRARIAN) on delete cascade " +
                    ")"
            );

            statement.executeUpdate("create trigger order_trigger " +
                    "before insert on ORDERS " +
                    "referencing new as new_order " +
                    "for each row " +
                    "begin " +
                    "if(:new_order.date_order is null) then " +
                    "select current_date into :new_order.date_order from DUAL;" +
                    "end if;" +
                    "end;");

            statement.executeUpdate("create sequence comp_seq start with 1 increment by 1 nomaxvalue ");
            statement.executeUpdate("create trigger comp_trigger " +
                    "before insert on COMPOSITIONS " +
                    "referencing new as new_comp " +
                    "for each row " +
                    "begin " +
                    "if(:new_comp.id_record is null) then " +
                    "select comp_seq.nextval into :new_comp.id_record from DUAL;" +
                    "end if;" +
                    "end;");
            statement.executeUpdate(
                    "create table Issued_books(" +
                            "id_record integer primary key," +
                            "id_librarian integer not null," +
                            "id_edition integer not null," +
                            "id_composition integer not null," +
                            "id_reader integer not null," +
                            "date_of_issue date," +
                            "return_date date," +
                            "is_returned varchar(6)," +
                            "check ( is_returned = 'да' or is_returned = 'нет' )," +
                            "foreign key (id_librarian) references LIBRARIANS(ID_LIBRARIAN) on delete cascade ," +
                            "foreign key (id_composition, id_edition) references COMPOSITIONS(ID_RECORD, ID_EDITION) on delete cascade ," +
                            "foreign key (id_reader) references READERS(id_reader) on delete cascade " +
                            ")"
            );
            statement.executeUpdate("create sequence issued_seq start with 1 increment by 1");
            statement.executeUpdate("create trigger issued_trigger " +
                    "before insert on ISSUED_BOOKS " +
                    "referencing new as new_issued " +
                    "for each row " +
                    "begin " +
                    "if(:new_issued.id_record is null) then " +
                    "select issued_seq.nextval into :new_issued.id_record from DUAL;" +
                    "select 'нет' into :new_issued.is_returned from DUAL;" +
                    "end if;" +
                    "end;");
            //category
            statement.executeUpdate("create table Teachers(" +
                    "id_reader integer primary key," +
                    "id_university integer not null ," +
                    "check ( id_university > 0 )," +
                    "faculty varchar(100) not null ," +
                    "name_university varchar(100) not null ," +
                    "foreign key (id_reader) references Readers (id_reader) on delete cascade " +
                    ")"
            );
            statement.executeUpdate("create table Researchers(" +
                    "id_reader integer primary key," +
                    "id_university integer not null," +
                    "check ( id_university > 0 )," +
                    "address_university varchar(100) not null," +
                    "degree varchar(100) not null," +
                    "name_university varchar(200) not null," +
                    "foreign key (id_reader) references Readers(id_reader) on delete cascade " +
                    ")"
            );
            statement.executeUpdate("create table SchoolChild(" +
                    "id_reader integer primary key," +
                    "id_school integer not null," +
                    "check ( id_school > 0 )," +
                    "grade integer not null," +
                    "check ( grade >= 1 and grade <= 11 )," +
                    "name_school varchar(100) not null," +
                    "foreign key(id_reader) references Readers(id_reader) on delete cascade " +
                    ")"
            );
            statement.executeUpdate("create table Students(" +
                    "id_reader integer primary key," +
                    "id_university integer not null," +
                    "check ( id_university > 0 )," +
                    "faculty varchar(100) not null," +
                    "name_university varchar(100) not null," +
                    "foreign key(id_reader) references Readers(id_reader) on delete cascade " +
                    ")"
            );
            statement.executeUpdate("create table Pensioners (" +
                    "id_reader integer primary key," +
                    "id_pensioner integer unique," +
                    "check ( id_pensioner > 0 ), " +
                    "foreign key(id_reader) references Readers(id_reader) on delete cascade " +
                    ")"
            );
            statement.executeUpdate("create table Workers(" +
                    "id_reader integer primary key," +
                    "firm_address varchar(200)," +
                    "name_firm varchar(200)," +
                    "foreign key(id_reader) references Readers(id_reader) on delete cascade " +
                    ")"
            );
            statement.executeUpdate("create table Others(" +
                    "id_reader integer primary key," +
                    "foreign key (id_reader) references READERS(ID_READER) on delete cascade " +
                    ")"
            );

            statement.executeUpdate("insert into Libraries(QUANTITY_BOOKS, NAME) values (5, 'Центральная библиотека')");
            statement.executeUpdate("insert into Libraries(QUANTITY_BOOKS, NAME) values (10, 'Школьная библиотека')");
            statement.executeUpdate("insert into Libraries(QUANTITY_BOOKS, NAME) values (7, 'Детская библиотека')");
            statement.executeUpdate("insert into Libraries(QUANTITY_BOOKS, NAME) values (15, 'Научная библиотека')");
            statement.executeUpdate("insert into Libraries(QUANTITY_BOOKS, NAME) values (12, 'Общая библиотека')");
            statement.executeUpdate("insert into Libraries(QUANTITY_BOOKS, NAME) values (10, 'Пригородная библиотека')");

            statement.executeUpdate("insert into HALLS values (1,1)");
            statement.executeUpdate("insert into HALLS values (2,1)");
            statement.executeUpdate("insert into HALLS values (3,1)");
            statement.executeUpdate("insert into HALLS values (1,2)");
            statement.executeUpdate("insert into HALLS values (2,2)");
            statement.executeUpdate("insert into HALLS values (3,2)");
            statement.executeUpdate("insert into HALLS values (1,3)");
            statement.executeUpdate("insert into HALLS values (2,3)");
            statement.executeUpdate("insert into HALLS values (1,4)");
            statement.executeUpdate("insert into HALLS values (2,4)");
            statement.executeUpdate("insert into HALLS values (1,5)");
            statement.executeUpdate("insert into HALLS values (2,5)");
            statement.executeUpdate("insert into HALLS values (1,6)");

            statement.executeUpdate("insert into USERS(password, user_mod) values ('nsu', 'Администратор')");
            statement.executeUpdate("insert into USERS(password, user_mod) values ('loc', 'Администратор')");

            statement.executeUpdate("insert into USERS(password, user_mod) values ('abc', 'Библиотекарь')");
            statement.executeUpdate("insert into USERS(password, user_mod) values ('cda', 'Библиотекарь')");
            statement.executeUpdate("insert into USERS(password, user_mod) values ('efc', 'Библиотекарь')");
            statement.executeUpdate("insert into USERS(password, user_mod) values ('cgt', 'Библиотекарь')");
            statement.executeUpdate("insert into USERS(password, user_mod) values ('ffg', 'Библиотекарь')");

            statement.executeUpdate("insert into LIBRARIANS values (3, 2, 1)");
            statement.executeUpdate("insert into LIBRARIANS values (4, 3, 2)");
            statement.executeUpdate("insert into LIBRARIANS values (5, 4, 1)");
            statement.executeUpdate("insert into LIBRARIANS values (6, 6, 1)");
            statement.executeUpdate("insert into LIBRARIANS values (7, 1, 2)");

            statement.executeUpdate("insert into USERS(password, user_mod) values ('dfd', 'Читатель')");
            statement.executeUpdate("insert into USERS(password, user_mod) values ('fgh', 'Читатель')");
            statement.executeUpdate("insert into USERS(password, user_mod) values ('sdr', 'Читатель')");
            statement.executeUpdate("insert into USERS(password, user_mod) values ('dfdg', 'Читатель')");
            statement.executeUpdate("insert into USERS(password, user_mod) values ('dgd', 'Читатель')");
            statement.executeUpdate("insert into USERS(password, user_mod) values ('fghg', 'Читатель')");
            statement.executeUpdate("insert into USERS(password, user_mod) values ('rtyy', 'Читатель')");
            statement.executeUpdate("insert into USERS(password, user_mod) values ('sdd', 'Читатель')");
            statement.executeUpdate("insert into USERS(password, user_mod) values ('cvvc', 'Читатель')");
            statement.executeUpdate("insert into USERS(password, user_mod) values ('dfdfd', 'Читатель')");
            statement.executeUpdate("insert into USERS(password, user_mod) values ('sdrr', 'Читатель')");
            statement.executeUpdate("insert into USERS(password, user_mod) values ('sdses', 'Читатель')");
            statement.executeUpdate("insert into USERS(password, user_mod) values ('dfdy', 'Читатель')");

            statement.executeUpdate("insert into READERS values (8, 3, 'Иванов', 'Иван', 'Иванович','студент')");
            statement.executeUpdate("insert into READERS values (9, 4, 'Сабинина', 'Фросья', 'Афросьевна', 'учитель')");
            statement.executeUpdate("insert into READERS values (10, 1, 'Козырев', 'Леонид', 'Федорович', 'работник')");
            statement.executeUpdate("insert into READERS values (11, 1, 'Козлова','Анастасия','Викторовна', 'ученик')");
            statement.executeUpdate("insert into READERS values (12, 2, 'Сарычев', 'Генадий', 'Олегович', 'научный сотрудник')");
            statement.executeUpdate("insert into READERS values (13, 2, 'Зюбин', 'Александр', 'Викторович', 'пенсионер')");
            statement.executeUpdate("insert into READERS values (14, 1, 'Киреев', 'Евгений', 'Сергеевич', 'учитель')");
            statement.executeUpdate("insert into READERS values (15, 6, 'Хлиманкова', 'Галина', 'Игоревна', 'студент')");
            statement.executeUpdate("insert into READERS values (16, 1, 'Налепова', 'Анастасия', 'Олеговна', 'студент')");
            statement.executeUpdate("insert into READERS values (17, 3, 'Шарапов', 'Виктор', 'Геннадьевич', 'пенсионер')");
            statement.executeUpdate("insert into READERS values (18, 1, 'Киреева', 'Ирина', 'Витальевна', 'прочие')");
            statement.executeUpdate("insert into READERS values (19, 4, 'Сорокин', 'Борис', 'Сергеевич', 'пенсионер')");
            statement.executeUpdate("insert into READERS values (20, 2, 'Исмагилов', 'Тимур', 'Зинферович', 'учитель')");

            statement.executeUpdate("insert into TEACHERS values (8, 2300, 'ФФ', 'НГУ')");
            statement.executeUpdate("insert into TEACHERS values (9, 2300, 'ФИТ', 'НГУ')");
            statement.executeUpdate("insert into STUDENTS values (10, 2300, 'ФИТ', 'НГУ')");
            statement.executeUpdate("insert into STUDENTS values (11, 3400, 'Экономический', 'НГУАЭ')");
            statement.executeUpdate("insert into STUDENTS values (12, 4500, 'Естественных наук', 'НГТУ')");
            statement.executeUpdate("insert into WORKERS values (13,'Пирогова 26','Data Science Center')");
            statement.executeUpdate("insert into SCHOOLCHILD(ID_READER, ID_SCHOOL, GRADE, NAME_SCHOOL) values (14, 367, 5, 'Лицей 45')");
            statement.executeUpdate("insert into RESEARCHERS(ID_READER, ID_UNIVERSITY, ADDRESS_UNIVERSITY, DEGREE, NAME_UNIVERSITY) values (15, 3400, 'Петрова 24', 'Кандидат наук', 'НГУАЭ')");
            statement.executeUpdate("insert into PENSIONERS(ID_READER, ID_PENSIONER) VALUES (16, 1678)");
            statement.executeUpdate("insert into PENSIONERS(ID_READER, ID_PENSIONER) VALUES (17, 56477)");
            statement.executeUpdate("insert into OTHERS(ID_READER) values (18)");
            statement.executeUpdate("insert into PENSIONERS(ID_READER, ID_PENSIONER) values (19, 78993)");
            statement.executeUpdate("insert into TEACHERS(ID_READER, ID_UNIVERSITY, FACULTY, NAME_UNIVERSITY) VALUES (20, 2300, 'ФИТ', 'НГУ')");

            statement.executeUpdate("insert into EDITIONS(ID_LIBRARY, HALL_NUM, RACK_NUM, SHELF_NUM, DATE_OF_ADMISSION, WRITE_OFF_DATE) values (2, 2, 12, 5, to_date('23.02.2021','dd.mm.yyyy'), to_date('14.07.2021','dd.mm.yyyy'))");
            statement.executeUpdate("insert into EDITIONS(ID_LIBRARY, HALL_NUM, RACK_NUM, SHELF_NUM, DATE_OF_ADMISSION, WRITE_OFF_DATE) values (4, 1, 15, 1, to_date('04.01.2020','dd.mm.yyyy'), to_date('06.12.2020','dd.mm.yyyy'))");
            statement.executeUpdate("insert into EDITIONS(ID_LIBRARY, HALL_NUM, RACK_NUM, SHELF_NUM, DATE_OF_ADMISSION, WRITE_OFF_DATE) values (1, 3, 5, 7, to_date('23.02.2020','dd.mm.yyyy'), to_date('21.07.2021','dd.mm.yyyy'))");
            statement.executeUpdate("insert into EDITIONS(ID_LIBRARY, HALL_NUM, RACK_NUM, SHELF_NUM, DATE_OF_ADMISSION, WRITE_OFF_DATE) values (1, 1, 23, 23, to_date('23.03.2020','dd.mm.yyyy'), to_date('02.08.2021','dd.mm.yyyy'))");
            statement.executeUpdate("insert into EDITIONS(ID_LIBRARY, HALL_NUM, RACK_NUM, SHELF_NUM, DATE_OF_ADMISSION, WRITE_OFF_DATE) values (6, 1, 57, 1, to_date('04.05.2020','dd.mm.yyyy'), to_date('06.08.2021','dd.mm.yyyy'))");
            statement.executeUpdate("insert into EDITIONS(ID_LIBRARY, HALL_NUM, RACK_NUM, SHELF_NUM, DATE_OF_ADMISSION, WRITE_OFF_DATE) values (4, 2, 24, 5, to_date('10.01.2021','dd.mm.yyyy'), to_date('27.08.2021','dd.mm.yyyy'))");
            statement.executeUpdate("insert into EDITIONS(ID_LIBRARY, HALL_NUM, RACK_NUM, SHELF_NUM, DATE_OF_ADMISSION, WRITE_OFF_DATE) values (5, 1, 665, 34, to_date('03.02.2021','dd.mm.yyyy'), to_date('08.09.2021','dd.mm.yyyy'))");
            statement.executeUpdate("insert into EDITIONS(ID_LIBRARY, HALL_NUM, RACK_NUM, SHELF_NUM, DATE_OF_ADMISSION, WRITE_OFF_DATE) values (2, 1, 34, 2, to_date('06.02.2021','dd.mm.yyyy'), to_date('07.10.2021','dd.mm.yyyy'))");
            statement.executeUpdate("insert into EDITIONS(ID_LIBRARY, HALL_NUM, RACK_NUM, SHELF_NUM, DATE_OF_ADMISSION, WRITE_OFF_DATE) values (1, 3, 5, 356, to_date('10.02.2021','dd.mm.yyyy'), to_date('15.10.2021','dd.mm.yyyy'))");
            statement.executeUpdate("insert into EDITIONS(ID_LIBRARY, HALL_NUM, RACK_NUM, SHELF_NUM, DATE_OF_ADMISSION, WRITE_OFF_DATE) values (1, 2, 1342, 23, to_date('05.03.2021','dd.mm.yyyy'), to_date('22.12.2021','dd.mm.yyyy'))");
            statement.executeUpdate("insert into EDITIONS(ID_LIBRARY, HALL_NUM, RACK_NUM, SHELF_NUM, DATE_OF_ADMISSION, WRITE_OFF_DATE) values (6, 1, 45, 235, to_date('13.03.2021','dd.mm.yyyy'), to_date('12.02.2022','dd.mm.yyyy'))");
            statement.executeUpdate("insert into EDITIONS(ID_LIBRARY, HALL_NUM, RACK_NUM, SHELF_NUM, DATE_OF_ADMISSION, WRITE_OFF_DATE) values (3, 2, 46, 1, to_date('06.05.2021','dd.mm.yyyy'), to_date('09.09.2021','dd.mm.yyyy'))");


            statement.executeUpdate("insert into COMPOSITIONS(ID_EDITION, AUTHOR, TITLE, POPULARITY, GENRE) values (1, 'Булгаков И.О.', 'Особенности проблем психики', 0.53, 'Научно-популярная статья')");
            statement.executeUpdate("insert into COMPOSITIONS(ID_EDITION, AUTHOR, TITLE, POPULARITY, GENRE) values (2, 'Иванов И.Р.', 'Физика-2', 0.02, 'Методическое пособие')");
            statement.executeUpdate("insert into COMPOSITIONS(ID_EDITION, AUTHOR, TITLE, POPULARITY, GENRE) values (3, 'Киреев А.И.', 'ЭВМ и Архитектура компьютера', 0.78, 'Учебная литература')");
            statement.executeUpdate("insert into COMPOSITIONS(ID_EDITION, AUTHOR, TITLE, POPULARITY, GENRE) values (4, 'Киреев А.И.', 'ЭВМ и Архитектура компьютера', 0.59, 'Учебная литература')");
            statement.executeUpdate("insert into COMPOSITIONS(ID_EDITION, AUTHOR, TITLE, POPULARITY, GENRE) values (5, 'Пушкин А.С.', 'Евгений Онегин', 0.35, 'Роман')");
            statement.executeUpdate("insert into COMPOSITIONS(ID_EDITION, AUTHOR, TITLE, POPULARITY, GENRE) values (6, 'Кожанов И.А.', 'ДГМА', 0.001, 'Учебная литература')");
            statement.executeUpdate("insert into COMPOSITIONS(ID_EDITION, AUTHOR, TITLE, POPULARITY, GENRE) values (7, 'Доманова Е.Д.', 'Определенный интеграл', 0.24, 'Учебная литература')");
            statement.executeUpdate("insert into COMPOSITIONS(ID_EDITION, AUTHOR, TITLE, POPULARITY, GENRE) values (8, 'Демидович Е.П.', 'Сборник задач по мат анализу', 0.8, 'Учебная литература')");
            statement.executeUpdate("insert into COMPOSITIONS(ID_EDITION, AUTHOR, TITLE, POPULARITY, GENRE) values (9, 'Хрущев Д.С.', 'Размышления о вечном', 0.56, 'Сборник стихов')");
            statement.executeUpdate("insert into COMPOSITIONS(ID_EDITION, AUTHOR, TITLE, POPULARITY, GENRE) values (10, 'Воронова А.Ю.', 'Однажды в центре города', 0.68, 'Детектив')");
            statement.executeUpdate("insert into COMPOSITIONS(ID_EDITION, AUTHOR, TITLE, POPULARITY, GENRE) values (11, 'Хрущев Д.С.', 'Размышления о вечном', 0.56, 'Сборник стихов')");
            statement.executeUpdate("insert into COMPOSITIONS(ID_EDITION, AUTHOR, TITLE, POPULARITY, GENRE) values (12, 'Гетманов И.Н.', 'Энциклопедия для детей', 0.43, 'Энциклопедии')");


            statement.executeUpdate("insert into RULES(ID_EDITION, RULE_TEXT) values (1, 'Не выдается на руки')");
            statement.executeUpdate("insert into RULES(ID_EDITION, RULE_TEXT) values (2, 'Выдается на небольшой срок')");
            statement.executeUpdate("insert into RULES(ID_EDITION, RULE_TEXT) values (3, 'При невозврате накладывается штраф')");
            statement.executeUpdate("insert into RULES(ID_EDITION, RULE_TEXT) values (4, 'Не выдается на руки')");
            statement.executeUpdate("insert into RULES(ID_EDITION, RULE_TEXT) values (5, 'Требует выплаты залога')");
            statement.executeUpdate("insert into RULES(ID_EDITION, RULE_TEXT) values (6, 'При потере возместить стоимость киниги')");
            statement.executeUpdate("insert into RULES(ID_EDITION, RULE_TEXT) values (6, 'Выдается на небольшой срок')");
            statement.executeUpdate("insert into RULES(ID_EDITION, RULE_TEXT) values (1, 'Пометки в книге запрещены')");
            statement.executeUpdate("insert into RULES(ID_EDITION, RULE_TEXT) values (2, 'При невозврате накладывается штраф')");
            statement.executeUpdate("insert into RULES(ID_EDITION, RULE_TEXT) values (10, 'Требует выплаты залога')");


            statement.executeUpdate("insert into ISSUED_BOOKS(ID_LIBRARIAN, ID_EDITION, id_composition, ID_READER,  DATE_OF_ISSUE, RETURN_DATE) values (3, 8, 8, 8, to_date('12.02.2020','dd.mm.yyyy'),to_date('01.03.2020','dd.mm.yyyy'))");
            statement.executeUpdate("insert into ISSUED_BOOKS(ID_LIBRARIAN, ID_EDITION, id_composition, ID_READER, DATE_OF_ISSUE, RETURN_DATE) values (4, 3, 3, 14, to_date('04.11.2020','dd.mm.yyyy'),to_date('20.11.2020','dd.mm.yyyy'))");


            statement.executeUpdate("create role c##librarian");
            statement.executeUpdate("create role c##reader");

            statement.executeUpdate("grant select on COMPOSITIONS to c##reader");
            statement.executeUpdate("grant select on EDITIONS to c##reader");
            statement.executeUpdate("grant select on ORDERS to c##reader");
            statement.executeUpdate("grant insert, update on ORDERS to c##reader");

            statement.executeUpdate("grant insert, delete, update on COMPOSITIONS to c##librarian");
            statement.executeUpdate("grant insert, delete, update on EDITIONS to c##librarian");
            statement.executeUpdate("grant insert, delete, update on READERS to c##librarian");
            statement.executeUpdate("grant insert, delete, update on TEACHERS to c##librarian");
            statement.executeUpdate("grant insert, delete, update on STUDENTS to c##librarian");
            statement.executeUpdate("grant insert, delete, update on SCHOOLCHILD to c##librarian");
            statement.executeUpdate("grant insert, delete, update on RESEARCHERS to c##librarian");
            statement.executeUpdate("grant insert, delete, update on WORKERS to c##librarian");
            statement.executeUpdate("grant insert, delete, update on PENSIONERS to c##librarian");
            statement.executeUpdate("grant insert, delete, update on ORDERS to c##librarian");
            statement.executeUpdate("grant insert, delete, update on RULES to c##librarian");
            statement.executeUpdate("grant insert, delete, update on ISSUED_BOOKS to c##librarian");
            statement.executeUpdate("grant select on COMPOSITIONS to c##librarian");
            statement.executeUpdate("grant select on EDITIONS to c##librarian");
            statement.executeUpdate("grant select on HALLS to c##librarian");
            statement.executeUpdate("grant select on ISSUED_BOOKS to c##librarian");
            statement.executeUpdate("grant select on LIBRARIANS to c##librarian");
            statement.executeUpdate("grant select on LIBRARIES to c##librarian");
            statement.executeUpdate("grant select on ORDERS to c##librarian");
            statement.executeUpdate("grant select on OTHERS to c##librarian");
            statement.executeUpdate("grant select on PENSIONERS to c##librarian");
            statement.executeUpdate("grant select on READERS to c##librarian");
            statement.executeUpdate("grant select on RESEARCHERS to c##librarian");
            statement.executeUpdate("grant select on RULES to c##librarian");
            statement.executeUpdate("grant select on SCHOOLCHILD to c##librarian");
            statement.executeUpdate("grant select on STUDENTS to c##librarian");
            statement.executeUpdate("grant select on TEACHERS to c##librarian");
            statement.executeUpdate("grant select on WORKERS to c##librarian");

            statement.executeUpdate("create role c##admin");
            statement.executeUpdate("grant c##librarian to c##admin");
            statement.executeUpdate("grant insert, delete, update on HALLS to c##admin");
            statement.executeUpdate("grant insert, delete, update on USERS to c##admin");
            statement.executeUpdate("grant insert, delete, update on LIBRARIANS to c##admin");
            statement.executeUpdate("grant insert, delete, update on LIBRARIES to c##admin");

            statement.executeUpdate("commit ");
        }
        catch (SQLException e){
            e.printStackTrace();
            try {
                Statement statement = conn.createStatement();
                statement.executeUpdate("rollback ");
            }catch (SQLException exception){
                exception.printStackTrace();
            }
        }
    }
}
