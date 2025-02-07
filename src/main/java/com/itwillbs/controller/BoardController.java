package com.itwillbs.controller;

import java.io.File;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itwillbs.domain.BoardBean;
import com.itwillbs.domain.ImageBean;
import com.itwillbs.domain.MemberBean;
import com.itwillbs.domain.OneRoomBean;
import com.itwillbs.domain.PageBean;
import com.itwillbs.domain.qnaBean;
import com.itwillbs.mailtest.GoogleAuthentication;
import com.itwillbs.service.BoardService;
import com.itwillbs.service.MemberService;

@Controller
public class BoardController {
	@Inject
	private BoardService boardService;

	@Inject
	private MemberService memberService;

	@Resource(name = "uploadPath")
	private String uploadPath;

//	http://localhost:8080/myweb2/board/write
	@RequestMapping(value = "/board/write", method = RequestMethod.GET)
	public String write() {
		// /WEB-INF/views/board/writeForm.jsp
		return "board/writeForm";
	}

	@RequestMapping(value = "/writePro", method = RequestMethod.POST)
	public String writePro(OneRoomBean bb, Model model) {
		String[] fileList = bb.getFileList();
		for (String string : fileList) {
			System.out.println("FileList : " + string);
		}
		
		if (fileList == null || fileList.length < 1 || fileList[0] == "") {
			model.addAttribute("msg", "매물 사진을 하나 이상 등록해주세요.");
			return "msg";
		}

		System.out.println("방등록 카테고리 : " + bb.getCategory());
		System.out.println("프리미엄 등록 기간 : " + bb.getPremium_expiry_date());
		bb.setInclude_fees(bb.getInclude_feesArray());
		bb.setInclude_options(bb.getInclude_optionsArray());

		boardService.insertRoom(bb);

		return "redirect:/";
	}
	
	
	@RequestMapping(value = "/updateRoom",method = RequestMethod.GET )
	public String sellRoom(HttpServletRequest request, Model model, HttpSession session) {
		try {
			String seller_id = (String)session.getAttribute("seller_id");
			MemberBean mb = memberService.getMember2(seller_id);
			String is_confirm = "N";
			if (mb != null) {
				is_confirm = mb.getIs_confirm();//(String)session.getAttribute("is_confirm");
			}
			
			if ((String) request.getParameter("room_id") == null) {
				model.addAttribute("msg", "잘못된 요청입니다.");
				return "msg";
			}
			if(seller_id ==null) {
				model.addAttribute("msg", "잘못된 요청입니다.");
				return "msg";
			}
			if(is_confirm ==null || !is_confirm.equals("Y") ) {
				model.addAttribute("msg", "가입 승인 대기중입니다.");
				return "msg";
			}
			
			int room_id = Integer.parseInt(request.getParameter("room_id"));

			OneRoomBean ob = boardService.getRoom(room_id);
			List<ImageBean> ibList = boardService.getImage(room_id);
			
			model.addAttribute("ob", ob);
			model.addAttribute("ibList", ibList);



		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return "updateRoom";
	}
	
	@RequestMapping(value = "updateRoomPro", method = RequestMethod.POST)
	public String updateRoomPro(Model model,HttpServletRequest request, OneRoomBean ob) {
		
		OneRoomBean obck = boardService.boardCheck(ob);
		
		String[] fileList = ob.getFileList();
		
		if (fileList == null || fileList.length < 1 || fileList[0] == "") {
			model.addAttribute("msg", "매물 사진을 하나 이상 등록해주세요.");
			return "msg";
		}
		
		for (String string : fileList) {
			System.out.println("FileList : " + string);
		}

		ob.setInclude_fees(ob.getInclude_feesArray());
		ob.setInclude_options(ob.getInclude_optionsArray());
		
		if (obck != null) {
			boardService.updateRoom(ob);
			return "redirect:salesList";
		} else {
			model.addAttribute("msg", "입력하신 정보가 틀립니다.");
			return "updateRoom";
		}
	}
	
	
	
	
	

	@RequestMapping(value = "/findRooms", method = RequestMethod.GET)
	public String list(HttpServletRequest request, Model model, HttpSession session) {
		PageBean pb = new PageBean();
		if (request.getParameter("pageNum") != null) {
			pb.setPageNum(request.getParameter("pageNum"));
		} else {
			pb.setPageNum("1");
		}
		pb.setPageSize(9);
		pb.setCategory("OneRoom");


		List<OneRoomBean> roomList = boardService.getBoardList(pb);

		pb.setCount(boardService.getBoardCount(pb));

		model.addAttribute("roomList", roomList);
		model.addAttribute("pb", pb);

		// 방 리스트와 썸네일 정보 넘기기
		List<LinkedHashMap<String, Object>> obList = boardService.selectOneRoomThumbImg();
		for (Map<String, Object> map : obList) {
			System.out.println(map.get("room_id") + " " + map.get("subject") + " " + map.get("file_name"));
		}

		model.addAttribute("obList", obList);

		String id = (String) session.getAttribute("id");

		if (id != null) {
			List<MemberBean> wishList = memberService.getMemberWishList(id);
			model.addAttribute("wishList", wishList);
		}
		return "findRooms";
	}

	@RequestMapping(value = "/findOfficetel", method = RequestMethod.GET)
	public String officetelList(HttpServletRequest request, Model model, HttpSession session) {
		PageBean pb = new PageBean();
		if (request.getParameter("pageNum") != null) {
			pb.setPageNum(request.getParameter("pageNum"));
		} else {
			pb.setPageNum("1");
		}
		pb.setPageSize(9);
		pb.setCategory("Officetel");


		List<OneRoomBean> roomList = boardService.getBoardList(pb);

		pb.setCount(boardService.getBoardCount(pb));

		model.addAttribute("roomList", roomList);
		model.addAttribute("pb", pb);

		// 방 리스트와 썸네일 정보 넘기기
		List<LinkedHashMap<String, Object>> obList = boardService.selectThumbImage("Officetel");//boardService.selectOneRoomThumbImg();
		for (Map<String, Object> map : obList) {
			System.out.println(map.get("room_id") + " " + map.get("subject") + " " + map.get("file_name"));
		}

		model.addAttribute("obList", obList);

		String id = (String) session.getAttribute("id");

		if (id != null) {
			List<MemberBean> wishList = memberService.getMemberWishList(id);
			model.addAttribute("wishList", wishList);
		}
		return "findOfficetel";
	}

	@RequestMapping(value = "/findRooms-search", method = RequestMethod.GET)
	public String search(HttpServletRequest request, Model model, HttpSession session, OneRoomBean ob) {
		String id = (String) session.getAttribute("id");
		
		List<OneRoomBean> roomList = boardService.getSearchList(ob);

		model.addAttribute("roomList", roomList);
		
		// 방 리스트와 썸네일 정보 넘기기
		List<LinkedHashMap<String, Object>> obList = boardService.selectOneRoomThumbImg();
		for (Map<String, Object> map : obList) {
			System.out.println(map.get("room_id") + " " + map.get("subject") + " " + map.get("file_name"));
		}

		model.addAttribute("obList", obList);

		if (id != null) {
			List<MemberBean> wishList = memberService.getMemberWishList(id);
			model.addAttribute("wishList", wishList);
		}
		return "findRooms-search";
	}

	@RequestMapping(value = "/findOfficetel-search", method = RequestMethod.GET)
	public String findOfficetelSearch(HttpServletRequest request, Model model, HttpSession session, OneRoomBean ob) {
		String id = (String) session.getAttribute("id");

		List<OneRoomBean> roomList = boardService.getSearchList(ob);

		model.addAttribute("roomList", roomList);
		
		// 방 리스트와 썸네일 정보 넘기기
		List<LinkedHashMap<String, Object>> obList = boardService.selectOneRoomThumbImg();
		for (Map<String, Object> map : obList) {
			System.out.println(map.get("room_id") + " " + map.get("subject") + " " + map.get("file_name"));
		}

		model.addAttribute("obList", obList);

		if (id != null) {
			List<MemberBean> wishList = memberService.getMemberWishList(id);
			model.addAttribute("wishList", wishList);
		}
		return "findOfficetel-search";
	}

	@RequestMapping(value = "/findRooms-zzim", method = RequestMethod.GET)
	public String zzimList(HttpServletRequest request, Model model, HttpSession session) {
		String id = (String) session.getAttribute("id");

		PageBean pb = new PageBean();
		if (request.getParameter("pageNum") != null) {
			pb.setPageNum(request.getParameter("pageNum"));
		} else {
			pb.setPageNum("1");
		}
		pb.setPageSize(9);
		pb.setId(id);

		pb.setCount(boardService.getWishCount(id));
		List<OneRoomBean> roomList = boardService.getWishList(pb);

		model.addAttribute("roomList", roomList);
		model.addAttribute("pb", pb);
		
		// 방 리스트와 썸네일 정보 넘기기
		List<LinkedHashMap<String, Object>> obList = boardService.selectOneRoomThumbImg();
		for (Map<String, Object> map : obList) {
			System.out.println(map.get("room_id") + " " + map.get("subject") + " " + map.get("file_name"));
		}

		model.addAttribute("obList", obList);

		if (id != null) {
			List<MemberBean> wishList = memberService.getMemberWishList(id);
			model.addAttribute("wishList", wishList);
		}
		return "zzimList";
	}

	@RequestMapping(value = "/member_seller", method = RequestMethod.GET)
	public String member_seller(HttpServletRequest request, Model model, HttpSession session) {
		String seller_id = (String) session.getAttribute("seller_id");
		MemberBean mb = memberService.getMember2(seller_id);
		String is_confirm = "N";
		if (mb != null) {
			is_confirm = mb.getIs_confirm();//(String)session.getAttribute("is_confirm");
		}
		if (seller_id == null) {
			return "/login";
		}
		if(is_confirm ==null || !is_confirm.equals("Y") ) {
			model.addAttribute("msg", "가입 승인 대기중입니다.");
			return "msg";
		}

		OneRoomBean ob = new OneRoomBean();
		ob.setSeller_id(seller_id);
		ob.setCategory("OneRoom");
		int OneroomCount = boardService.getSalesCategoryCount(ob);
		model.addAttribute("OneroomCount", OneroomCount);

		ob.setCategory("Officetel");
		int OfficetelCount = boardService.getSalesCategoryCount(ob);
		model.addAttribute("OfficetelCount", OfficetelCount);

		List<OneRoomBean> roomList = boardService.sellerLatestBoard(seller_id);
		model.addAttribute("roomList", roomList);
		
		List<qnaBean> qnaList = boardService.qnaLatestBoard(seller_id);
		model.addAttribute("qnaList", qnaList);

		return "member_seller";
	}

	@RequestMapping(value = "/salesList", method = RequestMethod.GET)
	public String salesList(HttpServletRequest request, Model model, HttpSession session) {
		PageBean pb = new PageBean();
		if (request.getParameter("pageNum") != null) {
			pb.setPageNum(request.getParameter("pageNum"));
		} else {
			pb.setPageNum("1");
		}
		pb.setPageSize(10);

		String seller_id = (String) session.getAttribute("seller_id");
		pb.setSeller_id(seller_id);

		if (seller_id == null) {
			return "/login";
		}
		
		OneRoomBean ob = new OneRoomBean();
		ob.setSeller_id(seller_id);
		ob.setCategory("OneRoom");
		int OneroomCount = boardService.getSalesCategoryCount(ob);
		model.addAttribute("OneroomCount", OneroomCount);
		
		ob.setCategory("Officetel");
		int OfficetelCount = boardService.getSalesCategoryCount(ob);
		model.addAttribute("OfficetelCount", OfficetelCount);

		List<OneRoomBean> roomList = boardService.getSalesList(pb);

		pb.setCount(boardService.getSalesCount(pb));

		model.addAttribute("roomList", roomList);
		model.addAttribute("pb", pb);

		return "salesList";
	}
	
	@RequestMapping(value = "/updateSalesState", method = RequestMethod.GET)
	public String updateSalesState(HttpServletRequest request, OneRoomBean ob) {
		
		ob.setIs_selling("N");
		boardService.updateSalesState(ob);

		return "redirect:salesList";
	}
	
	@RequestMapping(value = "/deleteBoard", method = RequestMethod.GET)
	public String deleteBoard(HttpSession session, Model model, OneRoomBean ob) {
		String seller_id = (String)session.getAttribute("seller_id");
		ob.setSeller_id(seller_id);
		
		OneRoomBean obck = boardService.boardCheck(ob);
		
		if (obck != null) {
			boardService.deleteBoard(ob);
		} else {
			model.addAttribute("msg", "입력하신 정보가 틀립니다.");
		}
		return "redirect:salesList";

	}
	
	
	@RequestMapping(value = "/soldList", method = RequestMethod.GET)
	public String soldList(HttpServletRequest request, Model model, HttpSession session) {
		PageBean pb = new PageBean();
		if (request.getParameter("pageNum") != null) {
			pb.setPageNum(request.getParameter("pageNum"));
		} else {
			pb.setPageNum("1");
		}
		pb.setPageSize(10);
		
		String seller_id = (String) session.getAttribute("seller_id");
		pb.setSeller_id(seller_id);
		
		if (seller_id == null) {
			return "/login";
		}
		
		pb.setIs_selling('Y');
		int salesCount = boardService.getSalesCount(pb);
		model.addAttribute("salesCount", salesCount);

		pb.setIs_selling('N');
		int soldCount = boardService.getSalesCount(pb);
		model.addAttribute("soldCount", soldCount);
		
		List<OneRoomBean> roomList = boardService.getSalesList(pb);
		pb.setCount(boardService.getSalesCount(pb));
		model.addAttribute("roomList", roomList);
		model.addAttribute("pb", pb);
		
		return "soldList";
	}
	
	@RequestMapping(value = "/qnaList", method = RequestMethod.GET)
	public String qnaList(HttpServletRequest request, Model model, HttpSession session) {
		PageBean pb = new PageBean();
		if (request.getParameter("pageNum") != null) {
			pb.setPageNum(request.getParameter("pageNum"));
		} else {
			pb.setPageNum("1");
		}
		pb.setPageSize(15);
		
		String seller_id = (String) session.getAttribute("seller_id");
		pb.setSeller_id(seller_id);
		
		if (seller_id == null) {
			return "/login";
		}
		
		List<qnaBean> qnaList = boardService.getQnaBoard(pb);
		pb.setCount(boardService.getQnaBoardCount(pb));
		model.addAttribute("qnaList", qnaList);
		model.addAttribute("pb", pb);
		
		
		return "qnaList";
	}

//	http://localhost:8080/myweb2/board/fwrite
	@RequestMapping(value = "/board/fwrite", method = RequestMethod.GET)
	public String fwrite() {
		// /WEB-INF/views/board/fwriteForm.jsp
		return "board/fwriteForm";
	}

	@RequestMapping(value = "/board/fwritePro", method = RequestMethod.POST)
	public String fwritePro(HttpServletRequest request, MultipartFile file) throws Exception {
		// 업로드
		System.out.println(uploadPath);
		System.out.println("원본파일이름" + file.getOriginalFilename());
		// 월본실제파일 file.getBytes()

		// 중복을 피하기 위해서 업로드파일이름 => 램덤문자_업로드파일이름
		UUID uid = UUID.randomUUID();
		String saveName = uid.toString() + "_" + file.getOriginalFilename();
		System.out.println(saveName);

		// upload폴더에 복사
		File target = new File(uploadPath, saveName);
		FileCopyUtils.copy(file.getBytes(), target);

		// 자바빈 저장
		BoardBean bb = new BoardBean();
		bb.setName(request.getParameter("name"));
		bb.setPass(request.getParameter("pass"));
		bb.setSubject(request.getParameter("subject"));
		bb.setContent(request.getParameter("content"));
//		bb.setFile(업로드된 파일이름);
		bb.setFile(saveName);

		// 디비 insertBoard()
		boardService.insertBoard(bb);

		return "redirect:/board/list";
	}

	// /board/content?num=${bb.num}
//	http://localhost:8080/myweb2/board/content?num=${bb.num}
	@RequestMapping(value = "/board/content", method = RequestMethod.GET)
	public String content(HttpServletRequest request, Model model) {
		int num = Integer.parseInt(request.getParameter("num"));
		// num 글번호에 해당하는 게시판 글 조회 => BoardBean 담아서 옴
		BoardBean bb = boardService.getBoard(num);
		// bb를 담아서 content.jsp 이동
		model.addAttribute("bb", bb);
		// /WEB-INF/views/board/content.jsp
		return "board/content";
	}

	@RequestMapping(value = "/board/update", method = RequestMethod.GET)
	public String update(HttpServletRequest request, Model model) {
		int num = Integer.parseInt(request.getParameter("num"));
		// num 글번호에 해당하는 게시판 글 조회 => BoardBean 담아서 옴
		BoardBean bb = boardService.getBoard(num);
		// bb를 담아서 content.jsp 이동
		model.addAttribute("bb", bb);
		// /WEB-INF/views/board/content.jsp
		return "board/updateForm";
	}

	@RequestMapping(value = "/board/updatePro", method = RequestMethod.POST)
	public String updatePro(BoardBean bb, Model model) {
		BoardBean bb2 = boardService.numCheck(bb);

		if (bb2 != null) {
			boardService.updateBoard(bb);
			return "redirect:/board/list";
		} else {
			// 입력하신 정보가 틀립니다.
			model.addAttribute("msg", "입력하신 정보가 틀립니다.");
			// /WEB-INF/views/member/msg.jsp
			return "member/msg";
		}
	}

	@RequestMapping(value = "/board/delete", method = RequestMethod.GET)
	public String delete(HttpServletRequest request, Model model) {
		int num = Integer.parseInt(request.getParameter("num"));

		model.addAttribute("num", num);

		return "board/deleteForm";
	}

	@RequestMapping(value = "/board/deletePro", method = RequestMethod.POST)
	public String deletePro(BoardBean bb, Model model) {
		BoardBean bb2 = boardService.numCheck(bb);

		if (bb2 != null) {
			boardService.deleteBoard(bb);
			return "redirect:/board/list";
		} else {
			// 입력하신 정보가 틀립니다.
			model.addAttribute("msg", "입력하신 정보가 틀립니다.");
			// /WEB-INF/views/member/msg.jsp
			return "member/msg";
		}
	}

	@RequestMapping(value = "/detailView", method = RequestMethod.GET)
	public String detailView(HttpServletRequest request, Model model, HttpSession session) {
		try {
			if ((String) request.getParameter("room_id") == null) {
				model.addAttribute("msg", "잘못된 요청입니다22.");
				// /WEB-INF/views/member/msg.jsp
				return "msg";
			}
			int room_id = Integer.parseInt(request.getParameter("room_id"));
			System.out.println("매물상세 -> 요청 방 ID : " + room_id);

			OneRoomBean ob = boardService.getRoom(room_id);
			List<ImageBean> ibList = boardService.getImage(room_id);
			// ob를 담아서 detailView.jsp 이동
			model.addAttribute("ob", ob);
			model.addAttribute("ibList", ibList);
			
			String id = (String) session.getAttribute("id");
			
			if (id != null) {
				List<MemberBean> wishList = memberService.getMemberWishList(id);
				model.addAttribute("wishList", wishList);
			}


		} catch (Exception e) {
			e.printStackTrace();
		}
		return "detailView";
	}
	
	@RequestMapping(value = "/memberQnaList", method = RequestMethod.GET)
	public String memberQnaList(HttpSession session, qnaBean qb, Model model, HttpServletRequest request) {
		String id = (String) session.getAttribute("id");
		
		PageBean pb = new PageBean();
		if (request.getParameter("pageNum") != null) {
			pb.setPageNum(request.getParameter("pageNum"));
		} else {
			pb.setPageNum("1");
		}
		pb.setPageSize(10);

		pb.setSender(id);
		List<qnaBean> qbList = boardService.getMemberQna(pb);
		pb.setCount(boardService.getMemberQnaCount(pb));
		
		model.addAttribute("qbList", qbList);
		model.addAttribute("pb", pb);

		return "memberQnaList";
	}
	
	
	@RequestMapping(value = "/memberQnaAnswer", method = RequestMethod.GET)
	public String memberQnaAnswer(Model model, HttpSession session, qnaBean qb) {

		qb =  boardService.getQna(qb);
		model.addAttribute("qb", qb);
		
		return "memberQnaAnswer";
	}
	
	
	@RequestMapping(value = "/answer", method = RequestMethod.GET)
	public String answer(Model model, HttpSession session, qnaBean qb) {
		
		qb =  boardService.getQna(qb);
		model.addAttribute("qb", qb);
		
		return "answer";
	}
	
	@RequestMapping(value = "/answerPro", method = RequestMethod.GET)
	public String answerPro(Model model, HttpSession session, qnaBean qb) {

		qb.setAnswerYN("Y");
		boardService.answerQna(qb);
		
		return "redirect:qnaList";
	}

	@RequestMapping(value = "/mailpro", method = RequestMethod.POST)
	public String mailpro(HttpServletRequest request, Model model, HttpSession session) {
		String id = (String)session.getAttribute("id");

		String sender = "hyunjoon42311@gamil.com";
		String receiver = request.getParameter("receiver");
		String phone = request.getParameter("phone");
		String content = request.getParameter("content");
		String name = request.getParameter("name");
		String date1 = request.getParameter("date1");
		String room_id = request.getParameter("room_id");

		String mailText = "";
		mailText += "상담 예약 날짜 : " + date1 + "<br>";
		mailText += "문의자 성함 : " + name + "<br>";
		mailText += "문의자 연락처 : " + phone + "<br>";
		mailText += "상담 내용 : " + content + "<br>";

		qnaBean qb = new qnaBean();
		qb.setContent(request.getParameter("content"));
		qb.setPhone_number(phone);
		qb.setName(name);
		qb.setRoom_id(Integer.parseInt(room_id));
		qb.setSender(id);
		qb.setAnswerYN("N");

		boardService.insertqna(qb);

		try {
			// 서버정보를 => Properties 객체 저장
			Properties properties = System.getProperties();
			// gmail은 무조건 true 고정
			properties.put("mail.smtp.starttls.enable", "true");
			// smtp 서버주소
			properties.put("mail.smtp.host", "smtp.gmail.com");
			// auth gmail은 무조건 true 고정
			properties.put("mail.smtp.auth", "true");
			// gmail 포트
			properties.put("mail.smtp.port", "587");
			// 구글메일인증
			// 패키지 mailtest 파일이름 GoogleAuthentication
			Authenticator auth = new GoogleAuthentication();
			// 메일전송하는 역할을 하는 단위 Session객체 생성
			Session s = Session.getDefaultInstance(properties, auth);
			// Session 이용해서 전송할 Message객체생성
			Message message = new MimeMessage(s);
			Address sender_address = new InternetAddress(sender);
			Address receiver_address = new InternetAddress(receiver);
			message.setHeader("content-Type", "text/html; charset=UTF-8");
			message.setFrom(sender_address);
			message.addRecipient(Message.RecipientType.TO, receiver_address);
			message.setSubject("문의사항");
			message.setContent(mailText, "text/html; charset=UTF-8");
			message.setSentDate(new Date());
			// 메시지 전송
			Transport.send(message);

		} catch (Exception e) {

			e.printStackTrace();
		}

		model.addAttribute("msg", "문의가 정상적으로 접수되었습니다.");

		return "msg";
	}

	@ResponseBody // view가 아닌 data리턴
	@RequestMapping(value = "/addressSearch", method = RequestMethod.POST)
	public ResponseEntity<String> zzim(MemberBean mb, HttpSession session) {
		ResponseEntity<String> entity = null;
		String result = "";

		try {
			String id = (String) session.getAttribute("id");
			mb.setId(id);

			// 한글깨짐 방지를 위해 인코딩하기
			result = "검색 결과가 없습니다. 정확한 검색어를 입력해주세요.";
			result = URLEncoder.encode(result, "UTF-8");

			entity = new ResponseEntity<String>(result, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			entity = new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
		}
		return entity;
	}
	
	@ResponseBody
	@RequestMapping(value = "/reportPro", method = RequestMethod.POST)
	public ResponseEntity<String> reportPro(qnaBean qb,HttpSession session) {
		ResponseEntity<String> entity = null;
		
		try {
			String room_id = (String)session.getAttribute("room_id");
			boardService.insertReport(qb);
			System.out.println(room_id);
			
			entity = new ResponseEntity<String>(HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			entity = new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
		}
//		System.out.println(qb.getRoom_id());



		
		return entity;
	
	}
	
	@RequestMapping(value = "/sellRoom/payPro", method = RequestMethod.GET)
	public String payPro(OneRoomBean ob, Model model) {
		
		System.out.println("프리미엄 등록 기간 : " + ob.getPremium_expiry_date());
		String[] fileList = ob.getFileList();
		if (fileList == null || fileList.length < 1 || fileList[0] == "") {
			model.addAttribute("msg", "매물 사진을 하나 이상 등록해주세요.");
			return "msg";
		}
		boardService.insertRoom(ob);

		return "redirect:/";
	}

}

