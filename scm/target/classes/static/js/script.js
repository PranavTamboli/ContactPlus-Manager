const { response } = require("express");

console.log("this is script js")
const toggleSidebar = () =>{
	if($(".sidebar").is(":visible")){
		/*true  */
		/*band karna hai */
		$(".sidebar").css("display", "none");
		$(".content").css("margin-left", "0%");
	}else{
		/*false */
	   /* show karna hai */	
		$(".sidebar").css("display", "block");
		$(".content").css("margin-left", "20%");
		
	}
};

const search = () => {
	/*console.log("Searching....");*/
	/*Value mil jayegi jb search karenge*/
	let query = $("#search-input").val();
	
	if(query === ""){
		$(".search-result").hide();
	}else{
	/*	$(".search-result").show();*/
		
			console.log(query);
	
					/*Sending Request To Server*/
	let url = `http://localhost:8181/search/${query}`;
	
	fetch(url)
		.then((response) => {
			return response.json();
		})
		.then((data) => {
			/* Data is comming*/
			console.log(data);
		});
		
		$(".search-result").show();
	}
};


