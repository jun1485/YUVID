import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.MenuKeyEvent;
import javax.swing.table.DefaultTableModel;

class cn { // connect
	static int userno = -1;
	static boolean ismanage = false;
	static boolean condition = false; // 예약취소시 true, 회원정보 수정시 false, 비밀번호 분실시 true
	static Connection conn = null;
	static Statement stmt = null;
	static PreparedStatement pstmt = null;
	static ResultSet rset = null;
}

@SuppressWarnings("serial")
class Login_Panel extends JPanel { // 1번째 패널 (로그인)
	private Sw_Project win;
	private static JTextField id_tf = new JTextField(30);
	private static JPasswordField pw_pf = new JPasswordField();
	private static final JLabel label1 = new JLabel("백신 예약 프로그램");
	private static final JLabel label2 = new JLabel("YUVID");
	private static final JLabel label3 = new JLabel("ID");
	private static final JLabel label4 = new JLabel("PW");
	private static final JButton reg_btn = new JButton("회원가입");
	private static final JButton find_id_btn = new JButton("ID찾기");
	private static final JButton find_pw_btn = new JButton("PW찾기");
	private static final JButton login_btn = new JButton("로그인");
	private static final JButton commu_btn = new JButton("게시판");

	public Login_Panel(Sw_Project win) { // UI구성
		this.win = win;
		setLayout(null);

		label1.setFont(new Font("Serif", Font.BOLD, 23));
		label1.setBounds(80, 40, 400, 50);
		add(label1);

		label2.setFont(new Font("Serif", Font.BOLD, 23));
		label2.setBounds(145, 70, 120, 65);
		add(label2);

		label3.setBounds(50, 170, 67, 15);
		add(label3);

		id_tf.setBounds(85, 167, 146, 21);
		add(id_tf);
		id_tf.setColumns(10);

		label4.setBounds(45, 204, 67, 15);
		add(label4);

		pw_pf.setBounds(85, 201, 146, 21);
		add(pw_pf);

		login_btn.setSize(86, 31);
		login_btn.setLocation(245, 178);
		add(login_btn);

		reg_btn.setSize(100, 23);
		reg_btn.setLocation(20, 270);
		add(reg_btn);

		find_id_btn.setSize(100, 23);
		find_id_btn.setLocation(132, 270);
		add(find_id_btn);

		find_pw_btn.setSize(100, 23);
		find_pw_btn.setLocation(245, 270);
		add(find_pw_btn);

		commu_btn.setSize(162, 31);
		commu_btn.setLocation(98, 338);
		add(commu_btn);

		reg_btn.addActionListener(new Reg_Action());
		find_id_btn.addActionListener(new Find_ID_Action());
		find_pw_btn.addActionListener(new Find_PW_Action());
		login_btn.addActionListener(new Login_Action());
		commu_btn.addActionListener(new Commu_Action());

		pw_pf.addActionListener(new Login_Action());
		id_tf.setFocusTraversalKeysEnabled(false);
		id_tf.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == MenuKeyEvent.VK_TAB)
					pw_pf.requestFocus();
			}
		});

	}

	private class Commu_Action implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			win.change("panel15");
		}
	}

	private class Reg_Action implements ActionListener { // 회원가입버튼
		@Override
		public void actionPerformed(ActionEvent e) {
			win.change("panel02");
			clear_field();
		}
	}

	private class Find_ID_Action implements ActionListener { // 아이디찾기 버튼
		@Override
		public void actionPerformed(ActionEvent e) {
			win.change("panel03");
			clear_field();
		}
	}

	private class Find_PW_Action implements ActionListener { // 비밀번호찾기 버튼
		@Override
		public void actionPerformed(ActionEvent e) {
			win.change("panel04");
			clear_field();
		}
	}

	private class Login_Action implements ActionListener { // 로그인버튼
		public void actionPerformed(ActionEvent e) {
			String id = id_tf.getText();
			String pw = String.copyValueOf(pw_pf.getPassword());
			if (check_id_input(id)) {
				JOptionPane.showMessageDialog(null, "아이디를 입력해주세요.", "", 0);
				return;
			}
			if (check_pw_input(pw)) {
				JOptionPane.showMessageDialog(null, "비밀번호를 입력해주세요.", "", 0);
				return;
			}
			if (check_login(id, pw)) {
				if (cn.ismanage) {
					JOptionPane.showMessageDialog(null, "관리자 권한으로 로그인 되었습니다.", "", 1);
					win.change("panel06");
				} else {
					JOptionPane.showMessageDialog(null, "로그인 되었습니다.", "", 1);
					win.change("panel06");
				}
				clear_field();
			}
			return;
		}
	}

	private static boolean check_id_input(String str) { // ID 공백확인
		if (str.equals(""))
			return true;
		return false;
	}

	private static boolean check_pw_input(String str) { // PW 공백확인
		if (str.equals(""))
			return true;
		return false;
	}

	private static boolean check_login(String id, String pw) { // 아이디 존재 여부 및 관리자권한 확인
		final String sql = "select yvpw, yvtype, yvuserno from yv_user where yvid=?";
		try {
			cn.pstmt = cn.conn.prepareStatement(sql);
			cn.pstmt.setString(1, id);
			cn.rset = cn.pstmt.executeQuery();
			if (cn.rset.next()) {
				if (cn.rset.getString("yvpw").equals(pw)) {
					if (cn.rset.getInt("yvtype") == 1)
						cn.ismanage = true;
					else
						cn.ismanage = false;
					cn.userno = cn.rset.getInt("yvuserno");
					return true;
				} else {
					JOptionPane.showMessageDialog(null, "비밀번호가 일치하지 않습니다.", "", 2);
				}
			} else {
				JOptionPane.showMessageDialog(null, "존재하지 않는 아이디 입니다", "", 0);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return false;
	}

	private static void clear_field() { // 텍스트박스 초기화
		id_tf.setText("");
		pw_pf.setText("");
	}
}

@SuppressWarnings("serial")
class SignUp_Panel extends JPanel { // 2번째 패널(회원가입)
	private static String checked_id = null;
	private Sw_Project win;
	private static final JLabel label1 = new JLabel("ID");
	private static final JLabel label2 = new JLabel("PW");
	private static final JLabel label3 = new JLabel("PW확인");
	private static final JLabel label4 = new JLabel("이름");
	private static final JLabel label6 = new JLabel("전화번호");
	private static JTextField id_tf = new JTextField();
	private static JTextField name_tf = new JTextField();
	private static JTextField phone_tf = new JTextField();
	private static JPasswordField pw_pf = new JPasswordField();
	private static JPasswordField pw_confirm_pf = new JPasswordField();
	private static final JButton id_check_btn = new JButton("중복확인");
	private static final JButton ok_btn = new JButton("가입");
	private static final JButton back_btn = new JButton("취소");

	public SignUp_Panel(Sw_Project win) { // UI구성
		setLayout(null);
		this.win = win;

		label1.setBounds(78, 100, 67, 15);
		add(label1);

		id_tf.setBounds(103, 97, 140, 21);
		add(id_tf);
		id_tf.setColumns(10);

		label2.setBounds(70, 140, 67, 15);
		add(label2);

		pw_pf.setBounds(103, 137, 140, 21);
		add(pw_pf);

		label3.setBounds(47, 180, 67, 15);
		add(label3);

		pw_confirm_pf.setBounds(103, 177, 140, 21);
		add(pw_confirm_pf);

		id_check_btn.setSize(85, 19);
		id_check_btn.setLocation(260, 97);
		add(id_check_btn);

		label4.setBounds(68, 220, 67, 15);
		add(label4);

		name_tf.setBounds(103, 217, 140, 21);
		add(name_tf);
		name_tf.setColumns(10);

		label6.setBounds(40, 300, 117, 15);
		add(label6);

		phone_tf.setBounds(103, 297, 140, 21);
		add(phone_tf);
		phone_tf.setColumns(10);

		ok_btn.setSize(120, 25);
		ok_btn.setLocation(54, 360);
		add(ok_btn);

		back_btn.setSize(120, 25);
		back_btn.setLocation(190, 360);
		add(back_btn);

		ok_btn.addActionListener(new Reg_Action());
		back_btn.addActionListener(new Back_Action());
		id_check_btn.addActionListener(new Check_ID_Action());
	}

	private class Check_ID_Action implements ActionListener { // 중복 버튼
		@Override
		public void actionPerformed(ActionEvent e) {
			final String sql = "select 1 from sys.dual where exists (select * from yv_user where yvid=?)";
			String register_id = id_tf.getText();

			for (int i = 0; i < register_id.length(); i++) {
				if (register_id.charAt(i) < 0x30 || (register_id.charAt(i) > 0x39 && register_id.charAt(i) < 0x41)
						|| (register_id.charAt(i) > 0x5A && register_id.charAt(i) < 0x61)
						|| register_id.charAt(i) > 0x7A) {
					JOptionPane.showMessageDialog(null, "아이디는 영문, 숫자만 사용 가능합니다.", "", 0);
					return;
				}
			}
			if (id_tf.getText().equals("")) {
				JOptionPane.showMessageDialog(null, "아이디를 입력해주세요.", "", 0);
			} else if (register_id.length() > 30) {
				JOptionPane.showMessageDialog(null, "아이디가 너무 깁니다.", "", 0);
			} else if (register_id.length() < 5) {
				JOptionPane.showMessageDialog(null, "아이디가 너무 짧습니다.", "", 0);
			} else {
				try {
					cn.pstmt = cn.conn.prepareStatement(sql);
					cn.pstmt.setString(1, register_id);
					cn.rset = cn.pstmt.executeQuery();
					if (cn.rset.next()) {
						JOptionPane.showMessageDialog(null, "존재하는 아이디 입니다", "", 2);
					} else {
						JOptionPane.showMessageDialog(null, "생성 가능한 아이디 입니다.", "", 1);
						checked_id = register_id;
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			return;
		}
	}

	private class Reg_Action implements ActionListener { // 가입 버튼
		@Override
		public void actionPerformed(ActionEvent e) {
			String pw = String.copyValueOf(pw_pf.getPassword());
			String pw_confirm = String.copyValueOf(pw_confirm_pf.getPassword());
			String name = name_tf.getText();
			String phone = phone_tf.getText();
			if (pw.equals("") || pw_confirm.equals("") || name.equals("") || phone.equals("")) {
				JOptionPane.showMessageDialog(null, "모든 항목을 채워주세요.", "", 0);
				return;
			}

			if (checked_id.equals(id_tf.getText())) {
				if (pw.equals(pw_confirm)) {
					for (int i = 0; i < pw.length(); i++) {
						if (pw.charAt(i) < 0x21 || pw.charAt(i) > 0x7E) {
							JOptionPane.showMessageDialog(null, "비밀번호는 영문, 숫자 및 일부 특수문자만 사용 가능합니다.", "", 0);
							return;
						}
					}
					if (pw.length() < 4) {
						JOptionPane.showMessageDialog(null, "비밀번호가 너무 짧습니다.", "", 0);
						return;
					} else if (pw.length() > 20) {
						JOptionPane.showMessageDialog(null, "비밀번호가 너무 깁니다.", "", 0);
						return;
					} else {
						JOptionPane.showMessageDialog(null, "회원가입이 완료 되었습니다.", "", 1);
						insert_id(checked_id, pw, name, phone);
						clear_field();
						win.change("panel01");
					}
				} else {
					JOptionPane.showMessageDialog(null, "비밀번호 재입력이 올바르게 되지 않았습니다.", "", 2);
				}
			} else {
				JOptionPane.showMessageDialog(null, "아이디 중복확인을 해주세요", "", 2);
			}
			return;
		}
	}

	private class Back_Action implements ActionListener { // 뒤로 버튼
		@Override
		public void actionPerformed(ActionEvent e) {
			win.change("panel01");
			clear_field();
		}

	}

	private static void clear_field() { // 텍스트박스 초기화
		id_tf.setText("");
		pw_pf.setText("");
		pw_confirm_pf.setText("");
		name_tf.setText("");
		phone_tf.setText("");
	}

	private static void insert_id(String iid, String ipw, String iname, String iphone) {
		final String sql = "insert into YV_USER values(?, ?, ?, ?, ?, 0)";
		final String sql2 = "SELECT MAX(yvuserno) from YV_USER";
		try {
			cn.stmt = cn.conn.createStatement();
			cn.rset = cn.stmt.executeQuery(sql2);
			cn.rset.next();
			int no_tmp = cn.rset.getInt("MAX(yvuserno)") + 1;
			cn.pstmt = cn.conn.prepareStatement(sql);
			cn.pstmt.setInt(1, no_tmp);
			cn.pstmt.setString(2, iid);
			cn.pstmt.setString(3, ipw);
			cn.pstmt.setString(4, iname);
			cn.pstmt.setString(5, iphone);
			cn.pstmt.executeUpdate();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}

@SuppressWarnings("serial")
class Find_ID_Panel extends JPanel { // 3번째 패널(ID찾기)
	private static JTextField name_tf = new JTextField();
	private static JTextField phone_tf = new JTextField();
	private Sw_Project win;
	private static final JLabel label1 = new JLabel("이름:");
	private static final JLabel label2 = new JLabel("전화번호:");
	private static final JButton find_btn = new JButton("찾기");
	private static final JButton back_btn = new JButton("취소");

	public Find_ID_Panel(Sw_Project win) { // UI구성
		setLayout(null);
		this.win = win;
		label1.setBounds(84, 60, 77, 15);
		add(label1);

		name_tf.setBounds(123, 57, 160, 21);
		add(name_tf);
		name_tf.setColumns(10);

		label2.setBounds(60, 94, 107, 15);
		add(label2);

		phone_tf.setBounds(123, 91, 160, 21);
		add(phone_tf);

		find_btn.setSize(120, 25);
		find_btn.setLocation(50, 145);
		add(find_btn);

		back_btn.setSize(120, 25);
		back_btn.setLocation(190, 145);
		add(back_btn);

		back_btn.addActionListener(new Back_Action());
		find_btn.addActionListener(new Find_Action());

	}

	private class Back_Action implements ActionListener { // 뒤로 버튼
		@Override
		public void actionPerformed(ActionEvent e) {
			win.change("panel01");
			clear_field();
		}
	}

	private class Find_Action implements ActionListener { // 찾기 버튼
		@Override
		public void actionPerformed(ActionEvent e) {
			final String sql = "select yvid from yv_user where yvphone_number=? and yvname=?";
			String name = name_tf.getText();
			String phone_number = phone_tf.getText();
			if (name.equals("") || phone_number.equals("")) {
				JOptionPane.showMessageDialog(null, "이름과 전화번호를 모두 입력해주세요.", "", 0);
				return;
			}
			try {
				cn.pstmt = cn.conn.prepareStatement(sql);
				cn.pstmt.setString(1, phone_number);
				cn.pstmt.setString(2, name);
				cn.rset = cn.pstmt.executeQuery();
				if (cn.rset.next()) { // 기입한 정보와 일치하는 아이디가 있다면
					JOptionPane.showMessageDialog(null, "당신의 아이디는 " + cn.rset.getString("yvid") + " 입니다.", "", 1);
					clear_field();
					win.change("panel01");
				} else {
					JOptionPane.showMessageDialog(null, "입력하신 정보와 일치하는 계정이 없습니다", "", 2);
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			return;
		}
	}

	private static void clear_field() { // 텍스트박스 초기화
		name_tf.setText("");
		phone_tf.setText("");
	}
}

@SuppressWarnings("serial")
class Find_PW_Panel extends JPanel { // 4번째 패널(PW찾기)
	private static JTextField id_tf = new JTextField();
	private static JTextField name_tf = new JTextField();
	private static JTextField phone_tf = new JTextField();
	private Sw_Project win;
	private static final JLabel label1 = new JLabel("ID:");
	private static final JLabel label2 = new JLabel("이름:");
	private static final JLabel label3 = new JLabel("전화번호:");
	private static final JButton find_btn = new JButton("찾기");
	private static final JButton back_btn = new JButton("취소");

	public Find_PW_Panel(Sw_Project win) { // UI구성
		setLayout(null);
		this.win = win;

		label1.setBounds(98, 96, 77, 15);
		add(label1);

		id_tf.setBounds(123, 93, 160, 21);
		add(id_tf);
		id_tf.setColumns(10);

		label2.setBounds(84, 130, 77, 15);
		add(label2);

		name_tf.setBounds(123, 127, 160, 21);
		add(name_tf);
		name_tf.setColumns(10);

		label3.setBounds(59, 164, 107, 15);
		add(label3);

		phone_tf.setBounds(123, 161, 160, 21);
		add(phone_tf);

		find_btn.setSize(120, 25);
		find_btn.setLocation(50, 215);
		add(find_btn);

		back_btn.setSize(120, 25);
		back_btn.setLocation(190, 215);
		add(back_btn);

		back_btn.addActionListener(new Back_Action());
		find_btn.addActionListener(new Find_Action());

	}

	private class Back_Action implements ActionListener { // 뒤로 버튼
		@Override
		public void actionPerformed(ActionEvent e) {
			win.change("panel01");
			clear_field();
		}
	}

	private class Find_Action implements ActionListener { // 찾기 버튼
		@Override
		public void actionPerformed(ActionEvent e) {
			final String sql = "select yvuserno from yv_user where yvid=? and yvphone_number=? and yvname=?";
			String id = id_tf.getText();
			String name = name_tf.getText();
			String phone_number = phone_tf.getText();
			if (id.equals("") || name.equals("") || phone_number.equals("")) {
				JOptionPane.showMessageDialog(null, "빈칸없이 정보를 입력해주세요.", "", 0);
				return;
			}
			try {
				cn.pstmt = cn.conn.prepareStatement(sql);
				cn.pstmt.setString(1, id);
				cn.pstmt.setString(2, phone_number);
				cn.pstmt.setString(3, name);
				cn.rset = cn.pstmt.executeQuery();
				if (cn.rset.next()) { // 기입한 정보와 일치하는 아이디가 있다면
					cn.userno = cn.rset.getInt("yvuserno");
					JOptionPane.showMessageDialog(null, "비밀번호 설정 창으로 이동합니다.", "", 1);
					cn.condition = true;
					clear_field();
					win.change("panel07");
				} else {
					JOptionPane.showMessageDialog(null, "입력하신 정보와 일치하는 계정이 없습니다", "", 2);
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			return;
		}
	}

	private static void clear_field() { // 텍스트박스 초기화
		id_tf.setText("");
		name_tf.setText("");
		phone_tf.setText("");
	}
}

@SuppressWarnings("serial")
class Confirm_PW_Panel extends JPanel { // 5번째 패널 (비밀번호(본인)확인 화면)
	private Sw_Project win;
	private static JPasswordField pw_pf = new JPasswordField();
	private static final JLabel label1 = new JLabel("현재 비밀번호:");
	private static final JButton verify_btn = new JButton("확인");
	private static final JButton back_btn = new JButton("취소");

	public Confirm_PW_Panel(Sw_Project win) { // UI구성
		setLayout(null);
		this.win = win;

		label1.setBounds(45, 67, 107, 15);
		add(label1);

		pw_pf.setBounds(140, 64, 160, 21);
		add(pw_pf);
		pw_pf.setColumns(10);

		verify_btn.setSize(100, 25);
		verify_btn.setLocation(75, 150);
		add(verify_btn);

		back_btn.setSize(100, 25);
		back_btn.setLocation(190, 150);
		add(back_btn);
		back_btn.addActionListener(new Back_Action());
		verify_btn.addActionListener(new Verify_Action());
	}

	private class Back_Action implements ActionListener { // 뒤로 버튼
		@Override
		public void actionPerformed(ActionEvent e) {
			win.change("panel06");
			clear_field();
		}
	}

	private class Verify_Action implements ActionListener { // 확인 버튼
		@Override
		public void actionPerformed(ActionEvent e) {
			final String sql = "select yvpw from yv_user where yvuserno=?";
			String pw = String.copyValueOf(pw_pf.getPassword());
			if (pw.equals("")) {
				JOptionPane.showMessageDialog(null, "비밀번호를 입력해주세요.", "", 0);
				return;
			} else {
				try {
					cn.pstmt = cn.conn.prepareStatement(sql);
					cn.pstmt.setInt(1, cn.userno);
					cn.rset = cn.pstmt.executeQuery();
					if (cn.rset.next()) {
						if (cn.rset.getString("yvpw").equals(pw)) {
							if (cn.condition)
								win.change("panel09");
							else
								win.change("panel07");
							clear_field();
							return;
						}
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			JOptionPane.showMessageDialog(null, "비밀번호가 일치하지 않습니다.", "", 2);
			return;
		}
	}

	private static void clear_field() { // 텍스트박스 초기화
		pw_pf.setText("");
	}

}

@SuppressWarnings("serial")
class MainPanel extends JPanel { // 6번째 패널 (메인메뉴)
	private Sw_Project win;
	private static final String sql1 = "SELECT hospitalno, adate FROM appoint WHERE yvuserno = ?";
	private static final JButton user_mod_btn = new JButton("회원정보 수정");
	private static final JButton back_btn = new JButton("로그아웃");
	private static final JButton appoint_btn = new JButton("접종 예약");
	private static final JButton inquiry_btn = new JButton("접종 조회");
	private static final JButton cancel_btn = new JButton("접종 취소");
	private static final JButton hospital_mod_btn = new JButton("병원리스트 수정");
	private static final JButton vaccine_mod_btn = new JButton("백신 재고리스트 수정");
	private static final JButton commu_btn = new JButton("게시판");

	public MainPanel(Sw_Project win) { // UI구성
		setLayout(null);
		this.win = win;

		user_mod_btn.setSize(120, 20);
		user_mod_btn.setLocation(10, 15);
		add(user_mod_btn);

		back_btn.setSize(90, 20);
		back_btn.setLocation(268, 15);
		add(back_btn);

		appoint_btn.setSize(160, 28);
		appoint_btn.setLocation(98, 115);
		add(appoint_btn);

		inquiry_btn.setSize(160, 28);
		inquiry_btn.setLocation(98, 160);
		add(inquiry_btn);

		cancel_btn.setSize(160, 28);
		cancel_btn.setLocation(98, 205);
		add(cancel_btn);

		hospital_mod_btn.setSize(160, 28);
		hospital_mod_btn.setLocation(98, 250);
		add(hospital_mod_btn);

		vaccine_mod_btn.setSize(160, 28);
		vaccine_mod_btn.setLocation(98, 295);
		add(vaccine_mod_btn);

		commu_btn.setSize(160, 28);
		commu_btn.setLocation(98, 340);
		add(commu_btn);

		user_mod_btn.addActionListener(new User_Mod_Action());
		appoint_btn.addActionListener(new Appoint_Action());
		inquiry_btn.addActionListener(new Inquiry_Action());
		back_btn.addActionListener(new Back_Action());
		hospital_mod_btn.addActionListener(new Hospital_Mod_Action());
		vaccine_mod_btn.addActionListener(new Vaccine_Mod_Action());
		cancel_btn.addActionListener(new Cancel_Action());
		commu_btn.addActionListener(new Commu_Action());

	}

	void menu_manage() {
		if (cn.ismanage) {
			vaccine_mod_btn.setVisible(true);
			hospital_mod_btn.setVisible(true);
		} else {
			vaccine_mod_btn.setVisible(false);
			hospital_mod_btn.setVisible(false);
		}
	}

	private class Commu_Action implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			win.change("panel15");
		}
	}

	private class User_Mod_Action implements ActionListener { // 회원정보 수정 버튼
		@Override
		public void actionPerformed(ActionEvent e) {
			cn.condition = false;
			win.change("panel05");
		}
	}

	private class Appoint_Action implements ActionListener { // 예약 버튼
		@Override
		public void actionPerformed(ActionEvent e) {
			if (!is_appoint()) {
				win.change("panel08");
			} else
				JOptionPane.showMessageDialog(null, "예약내역이 있습니다.", "", 2);
		}
	}

	private class Inquiry_Action implements ActionListener { // 조회 버튼
		@Override
		public void actionPerformed(ActionEvent e) {
			if (is_appoint()) {
				cn.condition = false;
				win.change("panel09");
			} else
				JOptionPane.showMessageDialog(null, "예약내역이 없습니다.", "", 2);
		}
	}

	private class Back_Action implements ActionListener { // 뒤로 버튼
		@Override
		public void actionPerformed(ActionEvent e) {
			win.change("panel01");
		}
	}

	private class Hospital_Mod_Action implements ActionListener { // 병원 관리 버튼
		@Override
		public void actionPerformed(ActionEvent e) {
			win.change("panel12");
		}
	}

	private class Vaccine_Mod_Action implements ActionListener { // 백신 관리 버튼
		@Override
		public void actionPerformed(ActionEvent e) {
			win.change("panel13");
		}
	}

	private class Cancel_Action implements ActionListener { // 예약 취소 버튼
		@Override
		public void actionPerformed(ActionEvent e) {
			if (is_appoint()) {
				cn.condition = true;
				win.change("panel05");
			} else
				JOptionPane.showMessageDialog(null, "예약내역이 없습니다.", "", 2);
		}
	}

	private static boolean is_appoint() {
		try {
			cn.pstmt = cn.conn.prepareStatement(sql1);
			cn.pstmt.setInt(1, cn.userno);
			cn.rset = cn.pstmt.executeQuery();
			if (cn.rset.next())
				return true;
		} catch (Exception e) {
			System.out.println(e);
		}
		return false;
	}
}

@SuppressWarnings("serial")
class User_Modify_Panel extends JPanel { // 7번째 패널 (비밀번호 변경 및 회원정보 수정)
	private Sw_Project win;
	private static JPasswordField pw_pf = new JPasswordField();
	private static JPasswordField pw_confirm_pf = new JPasswordField();
	private static JTextField phone_tf = new JTextField();
	private static final JLabel label1 = new JLabel("새 비밀번호:");
	private static final JLabel label2 = new JLabel("새 비밀번호 확인:");
	private static final JLabel label3 = new JLabel("새 전화번호 입력:");
	private static JButton modify_btn = new JButton("변경");
	private static JButton back_btn = new JButton("취소");

	public User_Modify_Panel(Sw_Project win) { // UI구성
		setLayout(null);
		this.win = win;

		label1.setBounds(68, 67, 107, 15);
		add(label1);

		pw_pf.setBounds(145, 64, 160, 21);
		add(pw_pf);
		pw_pf.setColumns(10);

		label2.setBounds(40, 107, 107, 15);
		add(label2);

		pw_confirm_pf.setBounds(145, 104, 160, 21);
		add(pw_confirm_pf);

		label3.setBounds(40, 147, 107, 15);
		add(label3);

		phone_tf.setBounds(145, 144, 160, 21);
		add(phone_tf);
		phone_tf.setColumns(10);

		modify_btn.setSize(80, 25);
		modify_btn.setLocation(100, 190);
		add(modify_btn);

		back_btn.setSize(80, 25);
		back_btn.setLocation(190, 190);
		add(back_btn);

		back_btn.addActionListener(new Back_Action());
		modify_btn.addActionListener(new Modify_Action());
	}

	private class Back_Action implements ActionListener { // 뒤로 버튼
		@Override
		public void actionPerformed(ActionEvent e) {
			if (cn.condition)
				win.change("panel01");
			else
				win.change("panel06");
			clear_field();
		}
	}

	private class Modify_Action implements ActionListener { // 변경 버튼
		@Override
		public void actionPerformed(ActionEvent e) {
			String pw = String.copyValueOf(pw_pf.getPassword());
			String pw_confirm = String.copyValueOf(pw_confirm_pf.getPassword());
			String phone_num = phone_tf.getText();
			if (pw.equals("") && pw_confirm.equals("") && phone_num.equals("")) {
				JOptionPane.showMessageDialog(null, "변경된 사항이 없습니다.", "", 2);
				return;
			}

			if (pw.equals(pw_confirm)) {
				for (int i = 0; i < pw.length(); i++) {
					if (pw.charAt(i) < 0x21 || pw.charAt(i) > 0x7E) {
						JOptionPane.showMessageDialog(null, "비밀번호는 영문, 숫자 및 일부 특수문자만 사용 가능합니다.", "", 0);
						return;
					}
				}

				if (pw.length() == 0) {
					JOptionPane.showMessageDialog(null, "전화번호가 수정 되었습니다.", "", 1);
					set_phone(phone_num);
					clear_field();
					win.change("panel06");
					return;
				} else if (pw.length() < 4) {
					JOptionPane.showMessageDialog(null, "비밀번호가 너무 짧습니다.", "", 0);
					return;
				} else if (pw.length() > 20) {
					JOptionPane.showMessageDialog(null, "비밀번호가 너무 깁니다.", "", 0);
					return;
				} else {
					JOptionPane.showMessageDialog(null, "재설정한 비밀번호로 로그인 해주세요.", "", 1);
					if (!phone_num.equals(""))
						set_phone(phone_num);
					set_pw(pw);
					clear_field();
					win.change("panel01");
					return;
				}
			} else {
				JOptionPane.showMessageDialog(null, "비밀번호 재입력이 올바르게 되지 않았습니다.", "", 2);
			}
			return;
		}
	}

	private static void clear_field() { // 텍스트박스 초기화
		pw_pf.setText("");
		pw_confirm_pf.setText("");
		phone_tf.setText("");
	}

	private static void set_phone(String sphone) {
		final String sql = "update yv_user set yvphone_number = ? where yvuserno = ?";
		try {
			cn.pstmt = cn.conn.prepareStatement(sql);
			cn.pstmt.setString(1, sphone);
			cn.pstmt.setInt(2, cn.userno);
			cn.pstmt.executeUpdate();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private static void set_pw(String spw) {
		final String sql = "update yv_user set yvpw = ? where yvuserno = ?";
		try {
			cn.pstmt = cn.conn.prepareStatement(sql);
			cn.pstmt.setString(1, spw);
			cn.pstmt.setInt(2, cn.userno);
			cn.pstmt.executeUpdate();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	void is_lost() {
		if (cn.condition) {
			label3.setVisible(false);
			phone_tf.setVisible(false);
		} else {
			label3.setVisible(true);
			phone_tf.setVisible(true);
		}
	}
}

@SuppressWarnings("serial")
class JPanel08 extends JPanel { // 8번째 패널 (접종예약 화면)
	private Sw_Project win;
	private static JComboBox<String> region_cb = new JComboBox<String>();
	private static JComboBox<String> hospital_cb = new JComboBox<String>();
	private static JComboBox<String> vaccine_cb = new JComboBox<String>();
	private static ArrayList<Integer> idx = new ArrayList<Integer>();
	private static final JButton appoint_btn = new JButton("예약");
	private static final JButton back_btn = new JButton("취소");

	public JPanel08(Sw_Project win) {

		setLayout(null);
		this.win = win;

		final String sql = "select distinct hregion from hospital order by hregion";
		clear_reg();
		clear_hos();
		clear_vac();

		try {
			cn.stmt = cn.conn.createStatement();
			cn.rset = cn.stmt.executeQuery(sql);
			while (cn.rset.next()) {
				region_cb.addItem(cn.rset.getString("hregion"));
			}
		} catch (Exception e) {
			System.out.println(e);
		}

		region_cb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clear_hos();
				clear_vac();
				JComboBox<String> cb = (JComboBox) e.getSource();
				String choice = (String) cb.getSelectedItem();
				update_Hlist(choice);
			}
		});

		region_cb.setBounds(80, 67, 176, 21);
		add(region_cb);

		hospital_cb.setBounds(80, 104, 176, 21);
		add(hospital_cb);

		vaccine_cb.setBounds(56, 140, 224, 21);
		add(vaccine_cb);

		appoint_btn.setSize(60, 25);
		appoint_btn.setLocation(98, 190);
		add(appoint_btn);

		back_btn.setSize(60, 25);
		back_btn.setLocation(178, 190);
		add(back_btn);

		back_btn.addActionListener(new Back_Action());
		appoint_btn.addActionListener(new Appoint_Action());
	}

	private class Back_Action implements ActionListener { // 뒤로 버튼
		@Override
		public void actionPerformed(ActionEvent e) {
			clear_reg();
			clear_hos();
			clear_vac();
			win.change("panel06");
		}
	}

	private class Appoint_Action implements ActionListener { // 예약 버튼
		@Override
		public void actionPerformed(ActionEvent e) {
			/*
			 * final String sql1 =
			 * "select vvolume from vaccine where hospitalno = ? and vname = ? and vdate = ?"
			 * ; try { cn.pstmt = cn.conn.prepareStatement(sql1); cn.pstmt.setInt(1,
			 * appoint_helper.hno); cn.pstmt.setString(2, appoint_helper.vname);
			 * cn.pstmt.setString(3, appoint_helper.vdate); cn.rset =
			 * cn.pstmt.executeQuery(); cn.rset.next(); int volume =
			 * cn.rset.getInt("vvolume"); if (volume == 1) { final String sql2 =
			 * "delete from vaccine where hospitalno = ? and vname = ? and vdate = ?";
			 * cn.pstmt = cn.conn.prepareStatement(sql2); cn.pstmt.setInt(1,
			 * appoint_helper.hno); cn.pstmt.setString(2, appoint_helper.vname);
			 * cn.pstmt.setString(3, appoint_helper.vdate); cn.pstmt.executeUpdate(); } else
			 * { final String sql3 =
			 * "update vaccine set vvolume = ? where hospitalno = ? and vname = ? and vdate = ?"
			 * ; cn.pstmt = cn.conn.prepareStatement(sql3); cn.pstmt.setInt(2, volume - 1);
			 * cn.pstmt.setInt(2, appoint_helper.hno); cn.pstmt.setString(3,
			 * appoint_helper.vname); cn.pstmt.setString(4, appoint_helper.vdate);
			 * cn.pstmt.executeUpdate(); }
			 * 
			 * } catch (Exception ee) { System.out.println(ee); }
			 */
			clear_reg();
			clear_hos();
			clear_vac();

			win.change("panel06");
		}
	}

	private static void update_Hlist(String hre) {
		final String sql1 = "select hospitalno from hospital h where exists (select 1 from vaccine v where h.hospitalno = v.hospitalno) and hregion = ? order by hname";
		final String sql2 = "select hname, hcall_number from hospital where hospitalno = ?";
		ResultSet rs_tmp;
		try {
			cn.pstmt = cn.conn.prepareStatement(sql1);
			cn.pstmt.setString(1, hre);
			cn.rset = cn.pstmt.executeQuery();
			while (cn.rset.next()) {
				cn.pstmt = cn.conn.prepareStatement(sql2);
				int no_tmp = cn.rset.getInt("hospitalno");
				idx.add(no_tmp);
				cn.pstmt.setInt(1, no_tmp);
				rs_tmp = cn.pstmt.executeQuery();
				rs_tmp.next();
				hospital_cb.addItem(rs_tmp.getString("hname") + ", " + rs_tmp.getString("hcall_number"));
			}
		} catch (Exception e) {
			System.out.println(e);
		}

		hospital_cb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clear_vac();
				JComboBox<String> cb = (JComboBox) e.getSource();
				int choice = cb.getSelectedIndex();
				if (choice > 0)
					update_Vlist(idx.get(choice - 1).intValue());
			}
		});
	}

	private static void update_Vlist(int hno) {
		clear_vac();
		final String sql = "select vname, vdate from vaccine where hospitalno = ?";
		try {
			cn.pstmt = cn.conn.prepareStatement(sql);
			cn.pstmt.setInt(1, hno);
			cn.rset = cn.pstmt.executeQuery();
			while (cn.rset.next()) {
				vaccine_cb.addItem(cn.rset.getString("vname") + ", " + cn.rset.getString("vdate"));
			}
		} catch (Exception e) {
			System.out.println(e);
		}

		vaccine_cb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox<String> cb = (JComboBox) e.getSource();
				String choice = (String) cb.getSelectedItem();
				String[] strArr = choice.split(", ");
				appoint_helper.hno = hno;
				appoint_helper.vname = strArr[0];
				appoint_helper.vdate = strArr[1];
				appoint_btn.setEnabled(true);
			}
		});
	}

	private static void clear_reg() {
		region_cb.removeAllItems();
		region_cb.setModel(new DefaultComboBoxModel<String>() {
			boolean selectionAllowed = true;

			@Override
			public void setSelectedItem(Object anObject) {
				if (!"지역 선택".equals(anObject)) {
					super.setSelectedItem(anObject);
				} else if (selectionAllowed) {
					selectionAllowed = false;
					super.setSelectedItem(anObject);
				}
			}
		});
		region_cb.addItem("지역 선택");
	}

	private static void clear_hos() {
		appoint_btn.setEnabled(false);
		hospital_cb.removeAllItems();
		idx.clear();
		hospital_cb.setModel(new DefaultComboBoxModel<String>() {
			boolean selectionAllowed = true;

			@Override
			public void setSelectedItem(Object anObject) {
				if (!"병원 선택".equals(anObject)) {
					super.setSelectedItem(anObject);
				} else if (selectionAllowed) {
					selectionAllowed = false;
					super.setSelectedItem(anObject);
				}
			}
		});
		hospital_cb.addItem("병원 선택");
	}

	private static void clear_vac() {
		appoint_btn.setEnabled(false);
		vaccine_cb.removeAllItems();
		vaccine_cb.setModel(new DefaultComboBoxModel<String>() {
			boolean selectionAllowed = true;

			@Override
			public void setSelectedItem(Object anObject) {
				if (!"백신 선택".equals(anObject)) {
					super.setSelectedItem(anObject);
				} else if (selectionAllowed) {
					selectionAllowed = false;
					super.setSelectedItem(anObject);
				}
			}
		});
		vaccine_cb.addItem("백신 선택");
	}

	class appoint_helper {
		static int hno;
		static String vname, vdate;
	}
}

@SuppressWarnings("serial")
class JPanel09 extends JPanel { // 9번째 패널 (예약 조회 및 예약 취소)
	private Sw_Project win;
	private static final String sql1 = "SELECT hospitalno, adate FROM appoint WHERE yvuserno = ?";
	private static final String sql2 = "SELECT yvname FROM YV_USER WHERE yvuserno = ?";
	private static final String sql3 = "SELECT hname FROM hospital WHERE hospitalno = ?";
	private static JLabel label1 = new JLabel();
	private static JLabel label2 = new JLabel();
	private static JLabel label3 = new JLabel();
	private static JLabel label4 = new JLabel();
	private static final JLabel label5 = new JLabel("정말 취소하시겠습니까?");
	private static final JButton back_btn = new JButton("확인");
	private static final JButton confirm_btn = new JButton("예약취소");
	private static final JButton back2_btn = new JButton("돌아가기");

	public JPanel09(Sw_Project win) { // UI구성
		setLayout(null);
		this.win = win;
		label1.setFont(new Font("Serif", Font.BOLD, 20));
		label1.setHorizontalAlignment(JLabel.CENTER);
		label1.setBounds(-20, 20, 400, 50);
		add(label1);

		label2.setFont(new Font("Serif", Font.BOLD, 23));
		label2.setHorizontalAlignment(JLabel.CENTER);
		label2.setBounds(-20, 45, 400, 50);
		add(label2);

		label3.setFont(new Font("Serif", Font.BOLD, 23));
		label3.setHorizontalAlignment(JLabel.CENTER);
		label3.setBounds(-20, 70, 400, 50);
		add(label3);

		label4.setFont(new Font("Serif", Font.BOLD, 23));
		label4.setHorizontalAlignment(JLabel.CENTER);
		label4.setBounds(-20, 95, 400, 50);
		add(label4);

		label5.setFont(new Font("Serif", Font.BOLD, 27));
		label5.setHorizontalAlignment(JLabel.CENTER);
		label5.setBounds(-20, 135, 400, 50);
		add(label5);

		back_btn.setSize(80, 25);
		back_btn.setLocation(142, 165);
		add(back_btn);

		confirm_btn.setSize(85, 25);
		confirm_btn.setLocation(90, 205);
		add(confirm_btn);

		back2_btn.setSize(85, 25);
		back2_btn.setLocation(190, 205);
		add(back2_btn);

		back_btn.addActionListener(new MyActionListener());
		back2_btn.addActionListener(new MyActionListener());
		confirm_btn.addActionListener(new MyActionListener2());
	}

	void is_inquiry() {
		if (cn.condition) {
			back_btn.setVisible(false);
			label5.setVisible(true);
			back2_btn.setVisible(true);
			confirm_btn.setVisible(true);
		} else {
			back_btn.setVisible(true);
			label5.setVisible(false);
			back2_btn.setVisible(false);
			confirm_btn.setVisible(false);
		}
		try {
			cn.pstmt = cn.conn.prepareStatement(sql1);
			cn.pstmt.setInt(1, cn.userno);
			cn.rset = cn.pstmt.executeQuery();
			cn.rset.next();
			int no_tmp = cn.rset.getInt("hospitalno");
			Date appointdate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(cn.rset.getString("adate"));
			SimpleDateFormat format1 = new SimpleDateFormat("yyyy년 MM월 dd일");
			SimpleDateFormat format2 = new SimpleDateFormat("HH시 mm분");
			label3.setText(format1.format(appointdate));
			label4.setText(format2.format(appointdate) + "입니다");

			cn.pstmt = cn.conn.prepareStatement(sql2);
			cn.pstmt.setInt(1, cn.userno);
			cn.rset = cn.pstmt.executeQuery();
			cn.rset.next();
			label1.setText(cn.rset.getString("yvname") + "님의 접종일은");

			cn.pstmt = cn.conn.prepareStatement(sql3);
			cn.pstmt.setInt(1, no_tmp);
			cn.rset = cn.pstmt.executeQuery();
			cn.rset.next();
			label2.setText(cn.rset.getString("hname"));
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private class MyActionListener implements ActionListener { // 뒤로 버튼
		@Override
		public void actionPerformed(ActionEvent e) {
			win.change("panel06");
		}
	}

	private class MyActionListener2 implements ActionListener { // 예약취소 버튼
		@Override
		public void actionPerformed(ActionEvent e) {
			win.change("panel06");
		}
	}
}

@SuppressWarnings("serial")
class JPanel12 extends JPanel { // 12번째 패널 (병원리스트 수정)
	private Sw_Project win;
	private static ArrayList<String> locate_arr = new ArrayList<String>();
	private static final JLabel label1 = new JLabel("병원이름 입력");
	private static final JLabel label2 = new JLabel("전화번호 입력");
	private static final JButton back_btn = new JButton("뒤로");
	private static final JButton add_btn = new JButton("추가");
	private static final JButton del_btn = new JButton("삭제");

	public JPanel12(Sw_Project win) {

		setLayout(null);
		this.win = win;

		back_btn.setSize(70, 20);
		back_btn.setLocation(10, 15);
		add(back_btn);

		JComboBox<String> nameCombo = new JComboBox<String>();
		nameCombo.setBounds(113, 67, 140, 21);
		add(nameCombo);
		nameCombo.setModel(new DefaultComboBoxModel<String>() {
			boolean selectionAllowed = true;

			@Override
			public void setSelectedItem(Object anObject) {
				if (!"지역 선택".equals(anObject)) {
					super.setSelectedItem(anObject);
				} else if (selectionAllowed) {
					selectionAllowed = false;
					super.setSelectedItem(anObject);
				}
			}
		});
		nameCombo.addItem("지역 선택");
		/* locate.toArray(new String[locate.size()]) */

		label1.setBounds(25, 110, 150, 15);
		add(label1);

		JTextField input_hName = new JTextField();
		input_hName.setBounds(113, 107, 140, 21);
		add(input_hName);
		input_hName.setColumns(10);

		label2.setBounds(25, 155, 150, 15);
		add(label2);

		JTextField input_pNumber = new JTextField();
		input_pNumber.setBounds(113, 153, 140, 21);
		add(input_pNumber);
		input_pNumber.setColumns(10);

		add_btn.setSize(60, 23);
		add_btn.setLocation(112, 195);
		add(add_btn);

		del_btn.setSize(60, 23);
		del_btn.setLocation(192, 195);
		add(del_btn);

		back_btn.addActionListener(new MyActionListener3());

		add_btn.addActionListener(new MyActionListener2());

		del_btn.addActionListener(new MyActionListener1());

	}

	private class MyActionListener1 implements ActionListener { // 추가 버튼
		@Override
		public void actionPerformed(ActionEvent e) {
			win.change("panel06");
		}
	}

	private class MyActionListener2 implements ActionListener { // 삭제 버튼
		@Override
		public void actionPerformed(ActionEvent e) {
			win.change("panel06");
		}
	}

	private class MyActionListener3 implements ActionListener { // 뒤로 버튼
		@Override
		public void actionPerformed(ActionEvent e) {
			win.change("panel06");
		}
	}
}

@SuppressWarnings("serial")
class JPanel13 extends JPanel { // 13번째 (백신 재고리스트 수정)
	private Sw_Project win;
	private static JTextField volume_tf = new JTextField();
	private static JTextField input_time = new JTextField();
	private static ArrayList<String> locate_arr = new ArrayList<String>();
	private static ArrayList<String> hospital_arr = new ArrayList<String>();
	private static ArrayList<String> vaccine_arr = new ArrayList<String>();
	private static JComboBox<String> name_cb = new JComboBox<String>();
	private static JComboBox<String> hospital_cb = new JComboBox<String>();
	private static JComboBox<String> vaccine_cb = new JComboBox<String>();
	private static final JLabel lblLb1 = new JLabel("시간");
	private static final JLabel lblLb2 = new JLabel("재고");
	private static final JButton add_btn = new JButton("추가");
	private static final JButton del_btn = new JButton("삭제");
	private static final JButton back_btn = new JButton("뒤로");
	private static final JButton addVacType = new JButton("백신종류추가");

	public JPanel13(Sw_Project win) {

		setLayout(null);
		this.win = win;

		back_btn.setSize(70, 20);
		back_btn.setLocation(10, 15);
		add(back_btn);

		addVacType.setSize(115, 20);
		addVacType.setLocation(247, 15);
		add(addVacType);

		name_cb.setBounds(113, 67, 140, 21);
		add(name_cb);

		hospital_cb.setBounds(113, 107, 140, 21);
		add(hospital_cb);

		vaccine_cb.setBounds(113, 147, 140, 21);
		add(vaccine_cb);

		/*
		 * locate.toArray(new String[locate.size()]) hospital.toArray(new
		 * String[hospital.size()]) vaccine.toArray(new String[vaccine.size()])
		 */

		lblLb1.setBounds(80, 190, 150, 15);
		add(lblLb1);

		input_time.setBounds(113, 187, 140, 21);
		add(input_time);
		input_time.setColumns(10);

		lblLb2.setBounds(80, 230, 150, 15);
		add(lblLb2);

		volume_tf.setBounds(113, 227, 140, 21);
		add(volume_tf);
		volume_tf.setColumns(10);

		add_btn.setSize(60, 23);
		add_btn.setLocation(112, 272);
		add(add_btn);

		del_btn.setSize(60, 23);
		del_btn.setLocation(192, 272);
		add(del_btn);

		back_btn.addActionListener(new MyActionListener3());

		add_btn.addActionListener(new MyActionListener2());

		del_btn.addActionListener(new MyActionListener1());

		addVacType.addActionListener(new MyActionListener4());

	}

	private class MyActionListener1 implements ActionListener { // 추가 버튼
		@Override
		public void actionPerformed(ActionEvent e) {
			win.change("panel06");
		}
	}

	private class MyActionListener2 implements ActionListener { // 삭제 버튼
		@Override
		public void actionPerformed(ActionEvent e) {
			win.change("panel06");
		}
	}

	private class MyActionListener3 implements ActionListener { // 뒤로 버튼
		@Override
		public void actionPerformed(ActionEvent e) {
			win.change("panel06");
		}
	}

	private class MyActionListener4 implements ActionListener { // 백신종류 추가 버튼
		@Override
		public void actionPerformed(ActionEvent e) {
			win.change("panel14");
		}
	}
}

@SuppressWarnings("serial")
class JPanel14 extends JPanel { // 14번째 패널

	private Sw_Project win;
	private static JTextField vaccine_tf = new JTextField();
	private static final JLabel label1 = new JLabel("백신이름");
	private static final JButton add_btn = new JButton("추가");
	private static final JButton del_btn = new JButton("삭제");
	private static final JButton back_btn = new JButton("뒤로");

	public JPanel14(Sw_Project win) {

		setLayout(null);
		this.win = win;

		back_btn.setSize(70, 20);
		back_btn.setLocation(10, 15);
		add(back_btn);

		label1.setBounds(60, 190, 150, 15);
		add(label1);

		vaccine_tf.setBounds(125, 187, 160, 21);
		add(vaccine_tf);
		vaccine_tf.setColumns(10);

		add_btn.setSize(80, 23);
		add_btn.setLocation(98, 272);
		add(add_btn);

		del_btn.setSize(80, 23);
		del_btn.setLocation(188, 272);
		add(del_btn);

		back_btn.addActionListener(new MyActionListener3());

		add_btn.addActionListener(new MyActionListener1());

		del_btn.addActionListener(new MyActionListener2());

	}

	private class MyActionListener1 implements ActionListener { // 추가 버튼
		@Override
		public void actionPerformed(ActionEvent e) {
			final String sql = "insert into VACCINETYPE values(?)";
			try {
				cn.pstmt = cn.conn.prepareStatement(sql);
				cn.pstmt.setString(1, vaccine_tf.getText());
				cn.pstmt.executeUpdate();
			} catch (Exception ee) {
				System.out.println(ee);
			}
		}
	}

	private class MyActionListener2 implements ActionListener { // 삭제 버튼
		@Override
		public void actionPerformed(ActionEvent e) {
			final String sql = "delete from VACCINETYPE where vname = ?";
			try {
				cn.pstmt = cn.conn.prepareStatement(sql);
				cn.pstmt.setString(1, vaccine_tf.getText());
				cn.pstmt.executeUpdate();
			} catch (Exception ee) {
				System.out.println(ee);
			}
		}
	}

	private class MyActionListener3 implements ActionListener { // 뒤로 버튼
		@Override
		public void actionPerformed(ActionEvent e) {
			clear_field();
			win.change("panel13");
		}
	}

	private static void clear_field() { // 텍스트박스 초기화
		vaccine_tf.setText("");
	}

}

@SuppressWarnings("serial")
class Community_Panel extends JPanel {
	private Sw_Project win;
	private static final String[] colNames = new String[] { "백신종류", "제목", "작성자" };
	private static JTable table = new JTable();
	private static JTable table2 = new JTable();
	private static JTextField searchString = new JTextField();
	private static JScrollPane scrollPane = new JScrollPane();
	private static JComboBox<String> search_cb = new JComboBox<String>();
	private static final JButton search_btn = new JButton("검색");
	private static final JButton write_btn = new JButton("글작성");
	private static final JButton refresh_btn = new JButton("새로고침");
	private static final JButton back_btn = new JButton("나가기");

	private class CommunityVO {

		int num;
		String vname;
		String title;
		String content;
		int writer;

	}

	public Community_Panel(Sw_Project win) {
		setLayout(null);
		this.win = win;

		scrollPane.setBounds(8, 49, 350, 570);
		add(scrollPane);

		List<CommunityVO> list = select();
		Object[][] rowDatas = new Object[list.size()][colNames.length];

		for (int i = 0; i < list.size(); i++) {
			rowDatas[i] = new Object[] { list.get(i).vname, list.get(i).title, get_userid(list.get(i).writer) };
		}

		table.setModel(new DefaultTableModel(rowDatas, colNames) {
			boolean[] columnEditables = new boolean[] { false, false, false };

			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});

		table.getColumnModel().getColumn(0).setResizable(false);
		table.getColumnModel().getColumn(0).setPreferredWidth(70);
		table.getColumnModel().getColumn(1).setResizable(false);
		table.getColumnModel().getColumn(1).setPreferredWidth(180);
		table.getColumnModel().getColumn(2).setResizable(false);
		table.getColumnModel().getColumn(2).setPreferredWidth(50);

		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int rowNum = table.getSelectedRow();
				CommunityVO vos = new CommunityVO();
				vos = list.get(rowNum);
				new Post_View(vos);
			}
		});

		scrollPane.setViewportView(table);

		search_cb.setModel(new DefaultComboBoxModel<String>(new String[] { "제목", "내용", "백신종류" }));
		search_cb.setBounds(10, 632, 70, 21);
		add(search_cb);

		searchString.setBounds(92, 632, 175, 21);
		add(searchString);
		searchString.setColumns(10);

		search_btn.setBounds(275, 632, 80, 23);
		add(search_btn);
		search_btn.addActionListener(new Search_Action());

		write_btn.setBounds(12, 16, 110, 23);
		write_btn.addActionListener(new Write_Action());

		add(write_btn);

		refresh_btn.setBounds(128, 16, 110, 23);
		refresh_btn.addActionListener(new Refresh_Action());
		add(refresh_btn);

		back_btn.setBounds(244, 16, 110, 23);
		back_btn.addActionListener(new Back_Action());
		add(back_btn);

	}

	private class Back_Action implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (cn.userno == -1)
				win.change("panel01");
			else
				win.change("panel06");
		}
	}

	private class Refresh_Action implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			table.setVisible(false);
			table_refresh();
		}
	}

	private class Search_Action implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (searchString.getText().equals("")) {
				JOptionPane.showMessageDialog(null, "검색어를 입력해주세요.");
			} else {
				if (String.valueOf(search_cb.getSelectedItem()) == "제목") {
					search("ptitle", searchString.getText());
				} else if (String.valueOf(search_cb.getSelectedItem()) == "내용") {
					search("pcontent", searchString.getText());
				} else if (String.valueOf(search_cb.getSelectedItem()) == "백신종류") {
					search("vname", searchString.getText());
				}
			}

		}
	}

	private class Write_Action implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			new Post_Add();
		}
	}

	private void table_refresh() {
		scrollPane.setBounds(8, 49, 350, 570);
		add(scrollPane);

		List<CommunityVO> list = select();
		Object[][] rowDatas = new Object[list.size()][colNames.length];

		for (int i = 0; i < list.size(); i++) {
			rowDatas[i] = new Object[] { list.get(i).vname, list.get(i).title, get_userid(list.get(i).writer) };
		}

		table2.setModel(new DefaultTableModel(rowDatas, colNames) {
			boolean[] columnEditables = new boolean[] { false, false, false };

			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});

		table2.getColumnModel().getColumn(0).setResizable(false);
		table2.getColumnModel().getColumn(0).setPreferredWidth(70);
		table2.getColumnModel().getColumn(1).setResizable(false);
		table2.getColumnModel().getColumn(1).setPreferredWidth(180);
		table2.getColumnModel().getColumn(2).setResizable(false);
		table2.getColumnModel().getColumn(2).setPreferredWidth(50);

		table2.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int rowNum = table2.getSelectedRow();
				CommunityVO vos = new CommunityVO();
				vos = list.get(rowNum);

				new Post_View(vos);
			}
		});

		scrollPane.setViewportView(table2);

		setVisible(true);
	}

	private String get_userid(int userno) {
		String userid = "";
		final String sql = "select yvid from yv_user where yvuserno = ?";
		try {
			cn.pstmt = cn.conn.prepareStatement(sql);
			cn.pstmt.setInt(1, userno);
			cn.rset = cn.pstmt.executeQuery();
			if (cn.rset.next()) {
				userid = cn.rset.getString("yvid");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return userid;
	}

	private void insert(String vname, String title, String content) {
		final String sql1 = "insert into post values (?, ?, ?, ?, ?)";
		final String sql2 = "SELECT MAX(postno) from POST";
		try {
			cn.stmt = cn.conn.createStatement();
			cn.rset = cn.stmt.executeQuery(sql2);
			cn.rset.next();
			int no_tmp = cn.rset.getInt("MAX(postno)") + 1;
			cn.pstmt = cn.conn.prepareStatement(sql1);
			cn.pstmt.setInt(1, no_tmp);
			cn.pstmt.setString(2, vname);
			cn.pstmt.setString(3, title);
			cn.pstmt.setString(4, content);
			cn.pstmt.setInt(5, cn.userno);

			cn.pstmt.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return;
	}

	private void delete(CommunityVO vo) {
		final String sql = "delete from post where postno=?";

		try {
			cn.pstmt = cn.conn.prepareStatement(sql);
			cn.pstmt.setInt(1, vo.num);

			cn.pstmt.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return;
	}

	private List<CommunityVO> select() {
		final String sql = "select * from post order by postno desc";
		List<CommunityVO> sel_list = new ArrayList<CommunityVO>();

		try {
			cn.pstmt = cn.conn.prepareStatement(sql);
			cn.rset = cn.pstmt.executeQuery();

			while (cn.rset.next()) {
				CommunityVO vo = new CommunityVO();
				vo.num = cn.rset.getInt("postno");
				vo.vname = cn.rset.getString("vname");
				vo.title = cn.rset.getString("ptitle");
				vo.content = cn.rset.getString("pcontent");
				vo.writer = cn.rset.getInt("pwriter");

				sel_list.add(vo);

			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return sel_list;
	}

	private void search(String search, String searchString) {
		List<CommunityVO> list = new ArrayList<CommunityVO>();

		try {
			String sql = "select postno, vname, ptitle, pcontent, pwriter from post where " + search + " like '%"
					+ searchString + "%' order by postno desc";
			cn.pstmt = cn.conn.prepareStatement(sql);
			cn.rset = cn.pstmt.executeQuery();

			while (cn.rset.next()) {
				CommunityVO vo = new CommunityVO();
				vo.num = cn.rset.getInt("postno");
				vo.vname = cn.rset.getString("vname");
				vo.title = cn.rset.getString("ptitle");
				vo.content = cn.rset.getString("pcontent");
				vo.writer = cn.rset.getInt("pwriter");

				list.add(vo);

			}

			if (list.size() == 0) {
				JOptionPane.showMessageDialog(null, "해당하는 게시글이 없습니다.");
			} else {
				// new Community_Panel();
				new Post_Search(list);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public class Post_Search extends JFrame {
		private JTable table;
		private JTextField searchString;

		public Post_Search(List<CommunityVO> vos) {

			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

			Dimension win_size = getContentPane().getSize();

			int x_pos = (int) (screen.getWidth() / 2 - win_size.getWidth() / 2);
			int y_pos = (int) (screen.getHeight() / 2 - win_size.getHeight() / 2);

			setTitle("검색결과");
			setBounds(new Rectangle(x_pos - 190, y_pos - 350, 380, 700));
			getContentPane().setLayout(null);

			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setBounds(8, 49, 350, 570);
			getContentPane().add(scrollPane);

			List<CommunityVO> list = vos;

			String[] colNames = new String[] { "백신종류", "제목", "작성자" };
			Object[][] rowDatas = new Object[list.size()][colNames.length];

			for (int i = 0; i < list.size(); i++) {
				rowDatas[i] = new Object[] { list.get(i).vname, list.get(i).title, get_userid(list.get(i).writer) };
			}
			table = new JTable();
			table.setModel(new DefaultTableModel(rowDatas, colNames) {
				boolean[] columnEditables = new boolean[] { false, false, false };

				public boolean isCellEditable(int row, int column) {
					return columnEditables[column];
				}
			});
			table.getColumnModel().getColumn(0).setResizable(false);
			table.getColumnModel().getColumn(0).setPreferredWidth(70);
			table.getColumnModel().getColumn(1).setResizable(false);
			table.getColumnModel().getColumn(1).setPreferredWidth(200);
			table.getColumnModel().getColumn(2).setResizable(false);
			table.getColumnModel().getColumn(2).setPreferredWidth(50);

			table.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					int rowNum = table.getSelectedRow();
					CommunityVO vos = new CommunityVO();
					vos = list.get(rowNum);
					new Post_View(vos);
				}
			});
			scrollPane.setViewportView(table);

			JButton btnclose = new JButton("닫기");
			btnclose.setBounds(244, 16, 110, 23);
			btnclose.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
				}
			});
			getContentPane().add(btnclose);

			setVisible(true);
		}
	}

	private class Post_View extends JFrame {

		public Post_View(CommunityVO vo) {
			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

			Dimension win_size = getContentPane().getSize();

			int x_pos = (int) (screen.getWidth() / 2 - win_size.getWidth() / 2);
			int y_pos = (int) (screen.getHeight() / 2 - win_size.getHeight() / 2);

			setBounds(new Rectangle(x_pos - 190, y_pos - 350, 380, 700));
			setTitle("게시글");
			getContentPane().setLayout(null);

			JLabel vnameLabel = new JLabel("백신종류");
			vnameLabel.setBounds(17, 25, 100, 20);
			getContentPane().add(vnameLabel);

			JLabel vnameLabel_2 = new JLabel(vo.vname);
			vnameLabel_2.setBounds(85, 25, 100, 20);
			getContentPane().add(vnameLabel_2);

			JLabel titleLabel = new JLabel("제목");
			titleLabel.setBounds(27, 60, 100, 20);
			getContentPane().add(titleLabel);

			JLabel titleLabel_2 = new JLabel(vo.title);
			titleLabel_2.setBounds(85, 60, 200, 20);
			getContentPane().add(titleLabel_2);

			JLabel writerLabel = new JLabel("작성자");
			writerLabel.setBounds(22, 95, 100, 20);
			getContentPane().add(writerLabel);

			JLabel writerLabel_2 = new JLabel(get_userid(vo.writer));
			writerLabel_2.setBounds(85, 95, 100, 20);
			getContentPane().add(writerLabel_2);

			JLabel contentLabel = new JLabel("내용");
			contentLabel.setBounds(27, 130, 100, 20);
			getContentPane().add(contentLabel);

			JTextArea textArea = new JTextArea(vo.content);
			textArea.setLineWrap(true);
			textArea.setRows(5);
			textArea.setBounds(85, 130, 250, 460);
			getContentPane().add(textArea);
			textArea.setEditable(false);

			JButton btnDel = new JButton("글삭제");
			btnDel.setBounds(135, 620, 100, 23);
			btnDel.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (cn.userno == vo.writer || cn.userno == 0) {
						delete(vo);
						JOptionPane.showMessageDialog(null, "게시글 삭제가 완료되었습니다.", "", 1);
						setVisible(false);
						table.setVisible(false);
						table_refresh();
					} else {
						JOptionPane.showMessageDialog(null, "작성자 본인만 삭제할 수 있습니다.", "", 0);
					}
				}
			});
			getContentPane().add(btnDel);

			JButton btnClose = new JButton("닫기");
			btnClose.setBounds(250, 620, 100, 23);
			btnClose.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					win.change("panel15");
					setVisible(false);

				}
			});
			getContentPane().add(btnClose);

			setVisible(true);

		}

	}

	private class Post_Add extends JFrame {
		private static JTextField title_tf;
		private static JTextArea content_ta = new JTextArea();
		private static final JLabel vnameLabel = new JLabel("백신종류");
		private static final JLabel titleLabel = new JLabel("제목");
		private static final JLabel contentLabel = new JLabel("내용");
		private static final JButton btnWrite = new JButton("작성완료");

		public Post_Add() {
			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

			Dimension win_size = getContentPane().getSize();

			int x_pos = (int) (screen.getWidth() / 2 - win_size.getWidth() / 2);
			int y_pos = (int) (screen.getHeight() / 2 - win_size.getHeight() / 2);

			setBounds(new Rectangle(x_pos - 190, y_pos - 350, 380, 700));
			setTitle("글쓰기");
			getContentPane().setLayout(null);

			vnameLabel.setBounds(17, 25, 100, 20);
			getContentPane().add(vnameLabel);

			String[] aaaaa = { "화이자", "모더나", "아스트라제네카", "얀센" };
			JComboBox<String> combobox = new JComboBox<String>(aaaaa);
			combobox.setBounds(85, 25, 140, 21);
			getContentPane().add(combobox);

			titleLabel.setBounds(27, 60, 100, 20);
			getContentPane().add(titleLabel);

			title_tf = new JTextField();
			title_tf.setBounds(85, 60, 250, 21);
			getContentPane().add(title_tf);
			title_tf.setColumns(10);

			contentLabel.setBounds(27, 95, 100, 20);
			getContentPane().add(contentLabel);

			content_ta.setLineWrap(true);
			content_ta.setRows(5);
			content_ta.setBounds(85, 95, 250, 495);
			getContentPane().add(content_ta);

			btnWrite.setBounds(140, 620, 97, 23);
			btnWrite.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (title_tf.getText().equals("")) {
						JOptionPane.showMessageDialog(null, "제목을 입력해주세요.", "", 2);

					} else if (content_ta.getText().equals("")) {
						JOptionPane.showMessageDialog(null, "내용을 입력해주세요.", "", 2);
					} else {
						JOptionPane.showMessageDialog(null, "게시글이 등록되었습니다.", "", 1);
						insert(combobox.getSelectedItem().toString(), title_tf.getText(), content_ta.getText());
						setVisible(false);
						table.setVisible(false);
						table_refresh();

					}
					clear_field();
				}

			});
			getContentPane().add(btnWrite);

			JButton btnClose = new JButton("취소");
			btnClose.setBounds(245, 620, 97, 23);
			btnClose.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					win.change("panel15");
					setVisible(false);

				}
			});
			getContentPane().add(btnClose);

			setVisible(true);

		}

		private static void clear_field() { // 텍스트박스 초기화
			title_tf.setText("");
			content_ta.setText("");
		}
	}

	void is_login() {
		if (cn.userno == -1) {
			write_btn.setEnabled(false);
		} else {
			write_btn.setEnabled(true);
		}
	}
}

@SuppressWarnings("serial")
class Sw_Project extends JFrame {

	public Login_Panel yvPanel01 = null;
	public SignUp_Panel yvPanel02 = null;
	public Find_ID_Panel yvPanel03 = null;
	public Find_PW_Panel yvPanel04 = null;
	public Confirm_PW_Panel yvPanel05 = null;
	public MainPanel yvPanel06 = null;
	public User_Modify_Panel yvPanel07 = null;
	public JPanel08 yvPanel08 = null;
	public JPanel09 yvPanel09 = null;
	public JPanel12 yvPanel12 = null;
	public JPanel13 yvPanel13 = null;
	public JPanel14 yvPanel14 = null;
	public Community_Panel yvPanel15 = null;

	public void change(String panelName) { // 패널 변경 후 재설정

		if (panelName.equals("panel01")) {
			cn.userno = -1;
			getContentPane().removeAll();
			getContentPane().add(yvPanel01);
			revalidate();
			repaint();
		}

		else if (panelName.equals("panel03")) {
			getContentPane().removeAll();
			getContentPane().add(yvPanel03);
			revalidate();
			repaint();
		}

		else if (panelName.equals("panel04")) {
			getContentPane().removeAll();
			getContentPane().add(yvPanel04);
			revalidate();
			repaint();
		}

		else if (panelName.equals("panel05")) {
			getContentPane().removeAll();
			getContentPane().add(yvPanel05);
			revalidate();
			repaint();
		}

		else if (panelName.equals("panel06")) {
			yvPanel06.menu_manage();
			getContentPane().removeAll();
			getContentPane().add(yvPanel06);
			revalidate();
			repaint();
		}

		else if (panelName.equals("panel07")) {
			yvPanel07.is_lost();
			getContentPane().removeAll();
			getContentPane().add(yvPanel07);
			revalidate();
			repaint();
		}

		else if (panelName.equals("panel08")) {
			getContentPane().removeAll();
			getContentPane().add(yvPanel08);
			revalidate();
			repaint();
		}

		else if (panelName.equals("panel09")) {
			yvPanel09.is_inquiry();
			getContentPane().removeAll();
			getContentPane().add(yvPanel09);
			revalidate();
			repaint();
		}

		else if (panelName.equals("panel10")) {
			getContentPane().removeAll();
			getContentPane().add(yvPanel09);
			revalidate();
			repaint();
		}

		else if (panelName.equals("panel12")) {
			getContentPane().removeAll();
			getContentPane().add(yvPanel12);
			revalidate();
			repaint();
		}

		else if (panelName.equals("panel13")) {
			getContentPane().removeAll();
			getContentPane().add(yvPanel13);
			revalidate();
			repaint();
		}

		else if (panelName.equals("panel14")) {
			getContentPane().removeAll();
			getContentPane().add(yvPanel14);
			revalidate();
			repaint();
		}

		else if (panelName.equals("panel15")) {
			yvPanel15.is_login();
			getContentPane().removeAll();
			getContentPane().add(yvPanel15);
			revalidate();
			repaint();
		}

		else {
			getContentPane().removeAll();
			getContentPane().add(yvPanel02);
			revalidate();
			repaint();
		}
	}

	public static void main(String[] args) {
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			cn.conn = DriverManager.getConnection("jdbc:oracle:thin:@//localhost:1521/xepdb1", "scott", "tiger");
		} catch (Exception e) {
			System.out.println(e);
		}
		Sw_Project win = new Sw_Project();

		win.setTitle("YUVID");
		win.yvPanel01 = new Login_Panel(win);
		win.yvPanel02 = new SignUp_Panel(win);
		win.yvPanel03 = new Find_ID_Panel(win);
		win.yvPanel04 = new Find_PW_Panel(win);
		win.yvPanel05 = new Confirm_PW_Panel(win);
		win.yvPanel06 = new MainPanel(win);
		win.yvPanel07 = new User_Modify_Panel(win);
		win.yvPanel08 = new JPanel08(win);
		win.yvPanel09 = new JPanel09(win);
		win.yvPanel12 = new JPanel12(win);
		win.yvPanel13 = new JPanel13(win);
		win.yvPanel14 = new JPanel14(win);
		win.yvPanel15 = new Community_Panel(win);

		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

		Dimension win_size = win.getSize();

		int x_pos = (int) (screen.getWidth() / 2 - win_size.getWidth() / 2);
		int y_pos = (int) (screen.getHeight() / 2 - win_size.getHeight() / 2);
		win.setLocation(x_pos - 190, y_pos - 350);

		win.add(win.yvPanel01);
		win.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		win.setSize(380, 700);
		win.setResizable(false);
		win.setVisible(true);
	}
}
