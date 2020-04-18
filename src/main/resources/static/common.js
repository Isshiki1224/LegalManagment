// JavaScript Document

$(function(){		   
	$(".searListCon").find("dl:odd").css("background","#fff");
	$(".list").find(".listCon:odd").css("background","#fff");
	
	//ȫѡ
	$("#allCheckbox").click(function(){
		$('input[name="checkbox"]').attr("checked",this.checked);					 
	})
	var $checkbox = $("input[name='checkbox']");
	$checkbox.click(function(){
		$("#allCheckbox").attr("checked",$checkbox.length == $("input[name='checkbox']:checked").length ? true : false);
	});
	
	
	//���չ������
	$(".open").click(function(){
		$(this).each(function(){
			if($(this).is(":visible")){
				$(this).hide();
				$(this).next().show();
				$(this).parent().parent().parent().parent().parent().parent().next().hide();
				$(this).parent().parent().parent().parent().parent().parent().next().next().show();
				$(this).parent().parent().parent().parent().parent().parent().next().next().next().show();
			}
		})				
	})
	$(".close").click(function(){
		$(this).each(function(){
			if($(this).is(":visible")){
				$(this).hide();
				$(this).prev().show();
				$(this).parent().parent().parent().parent().parent().parent().next().show();
				$(this).parent().parent().parent().parent().parent().parent().next().next().hide();
				$(this).parent().parent().parent().parent().parent().parent().next().next().next().hide();
			}
		})				
	})		
	$(".openBut").click(function(){
		$(this).each(function(){
			if($(this).is(":visible")){
				$(this).hide();
				$(this).prev().find(".open").hide();
				$(this).prev().find(".close").show();
				$(this).next().show();
				$(this).next().next().show();
			}
		})				
	})
	$(".closeBut").click(function(){
		$(this).each(function(){
			if($(this).is(":visible")){
				$(this).hide();
				$(this).prev().prev().prev().find(".open").show();
				$(this).prev().prev().prev().find(".close").hide();
				$(this).prev().hide();
				$(this).prev().prev().show();
			}
		})				
	})
	$(".allOpen").click(function(){
		$(this).each(function(){
			if($(this).is(":visible")){
				$(".open").hide();
				$(".close").show();
				$(".openBut").hide();
				$(".openCon").show();
				$(".closeBut").show();
			}
		})				
	})
	
	
	
	
})
//��ȡʱ��
function tick(){
	var myDate = new Date();
	curYear = myDate.getFullYear();
	curMonth = myDate.getMonth()+1; //��ȡ��ǰ�·�(0-11,0����1��)
	curDateT = myDate.getDate(); //��ȡ��ǰ��(1-31)
	curDateH = myDate.getHours(); //��ȡ��ǰСʱ��(0-23)
	curDateM = myDate.getMinutes(); //��ȡ��ǰ������(0-59)
	curMonthN = curMonth<10 ? "0" + curMonth : curMonth;
	curDateTn = curDateT<10 ? "0" + curDateT : curDateT;
	curDateH = curDateH<10 ? "0" + curDateH : curDateH;
	curDateM = curDateM<10 ? "0" + curDateM : curDateM;
	$(".time").html(curYear+"."+curMonthN+"."+curDateTn+" "+curDateH+"."+curDateM);
	$(".timeEnglish").html(curYear+"."+curMonthN+"."+curDateTn+" "+curDateH+"."+curDateM);
}
window.onload=function(){
	tick();
}

//���������С
function fontResizer(smallFont,medFont,largeFont){

	function saveState(curSize){
		var date = new Date();
		date.setTime(date.getTime()+(7*24*60*60*1000));
		var expires = "; expires="+date.toGMTString();
		document.cookie = "fontSizer"+"="+curSize+expires+"; path=/";
	}

	$(".smallFont").click(function(){
		$('.con').css('font-size', smallFont);
		$(".smallFont").addClass("curFont");
		saveState(smallFont);
	});

	$(".medFont").click(function(){
		$('.con').css('font-size', medFont);
		$(".medFont").addClass("curFont");
		saveState(medFont);
	});

	$(".largeFont").click(function(){
		$('.con').css('font-size', largeFont);
		$(".largeFont").addClass("curFont");
		saveState(largeFont);
	});

	function getCookie(c_name){
		if(document.cookie.length>0){
			c_start=document.cookie.indexOf(c_name + "=");
			if (c_start!=-1){
				c_start=c_start + c_name.length+1;
				c_end=document.cookie.indexOf(";",c_start); 
				if(c_end==-1)c_end=document.cookie.length;
				return unescape(document.cookie.substring(c_start,c_end));
			}
		} 
		return "";
	}

	var savedSize = getCookie('fontSizer');

	if(savedSize!=""){
		$('.con').css('font-size', savedSize);
	}else {
		$('.con').css('font-size', medFont);
	}
	
}



$(function(){
	var width_view = $(window).outerWidth(); //���ӷ�Χ�Ŀ��
	var height_view = $(window).outerHeight(); //���ӷ�Χ�ĸ߶�
	
	var width_body = $("body").outerWidth(); //body���ݵĿ��
	var height_body = $("body").outerHeight(); //body���ݵĸ߶�
	
	var height = Math.max(height_view,height_body); //������ĸ߶�
	
	var htop=$(window).scrollTop() || $("body").scrollTop(); //��������ȥ�Ŀ��
	var hleft=$(window).scrollLeft()|| $("body").scrollLeft(); //��������ȥ�ĸ߶�
	
	
	$(".classBut").click(function(){
		
		
		var width_dialog = $('.classPopUp').outerWidth(); //������Ŀ��
		var height_dialog = $('.classPopUp').outerHeight(); //������ĸ߶�
		
		var left = (width_view - width_dialog)/2 + hleft;
		var top = (height_view - height_dialog)/2 + htop;
		$(".blackBox").show();
		var params = {top:top+'px',left:left+'px',display:'block'};

		$('.classPopUp').css(params).show();
	});
	
	$("span#classClose").live('click',function(){
		$(this).parents('.classPopUp').hide();	
		$(".blackBox").hide();
	})
	
	$(".showBut").click(function(){
		var width_dialog = $('.PopUp').outerWidth(); //������Ŀ��
		var height_dialog = $('.PopUp').outerHeight(); //������ĸ߶�
		
		var left = (width_view - width_dialog)/2 + hleft;
		var top = (height_view - height_dialog)/2 + htop;
		$(".blackBox").show();
		var params = {top:top+'px',left:left+'px',display:'block'};

		$('.PopUp').css(params).show();
	});
	
	$("span#showClose").live('click',function(){
		$(this).parents('.PopUp').hide();	
		$(".blackBox").hide();
	})
	
})

function addWaterMarker(str) {
    var can = document.createElement('canvas');
    var body = document.getElementById("water_div");
    body.appendChild(can);
    can.width = 245;
    can.height = 190;
    can.style.display = 'none';
    var cans = can.getContext('2d');
    cans.rotate(-20 * Math.PI / 180);
    cans.font = "16px Microsoft JhengHei";
    cans.fillStyle = "rgba(17, 17, 17, 0.50)";
    cans.textAlign = 'left';
    cans.textBaseline = 'Middle';
    cans.fillText(str, can.width / 3, can.height / 2);
    body.style.backgroundImage = "url(" + can.toDataURL("image/png") + ")";
}
