console.log("this is custom js");

const toggleSidebar = () => {
	console.log("onclick");
	if($( ".sidebar").is(":visible"))
	{
			console.log("true");
		$(".sidebar").css("display","none");
		$(".content").css("margin-left","0%")
	}
	else{
				console.log("false");
		$(".sidebar").css("display","block");
		$(".content").css("margin-left","20%")
	}
}