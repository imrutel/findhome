<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Findhome</title>
  <link rel="stylesheet" href="resources/css/admin_style.css">
</head>
<body>
  <section class="left_section">
    <h3>Findhome</h3>
    
    <ul>
      <li><a href=""><strong>대시보드</strong></a></li>
    </ul>
    <ul>
      <li><a href=""><strong>개인회원 관리</strong></a></li>
      <li><a href="">사용자 관리</a></li>
      <li><a href="">가입 승인</a></li>
      <li><a href="">문의글 관리</a></li>
      <li><a href="">통계</a></li>
    </ul>
    <ul>
      <li><a href=""><strong>사업자회원 관리</strong></a></li>
      <li><a href="">사용자 관리</a></li>
      <li><a href="">가입 승인</a></li>
      <li><a href="">문의글 관리</a></li>
      <li><a href="">통계</a></li>
    </ul>
  </section>
  
  
  <section class="right_section">
  
    <div class="header clear">
      <h4>개인회원 관리</h4> 
    </div>
    
    <div class="dashboard">
      <h5>Dashboard</h5>
      
      <div class="board_left">
        <div class="board_box">
          <p>승인대기 회원 목록</p>
          <c:forEach var="mb"  items="${nList}">
          <p>${mb.id} <input type="button"  value="승인버튼"  onclick="location.href='<c:url value="/yUpdate?id=${mb.id}" />' "></p>
          </c:forEach>
          </div>
         </div>
         
       <div class="board_center">  
        <div class="board_box">
          <p>매몰신고 리스트</p>      
        </div>
      </div>
      
      <div class="board_right">
        <div class="board_box">
          <p>문의글</p>
        </div>
      </div>
    </div>
  </section>
</body>
</html>