const toggleSidebar = () => {
    if ($(".sidebar").is(":visible")) {
        // true

        $(".sidebar").css("display", "none");
        $(".content").css("margin-left", "0%");
    } else {
        // false
        $(".sidebar").css("display", "block");
        $(".content").css("margin-left", "20%");

    }
};

const search = () => {
    // console.log("Searching....")

    let query = $("#search-input").val();
    // console.log(query)

    if (query === "") {
        $(".search-result").hide();
    }
    else {
        console.log(query);

        let url = `http://localhost:8081/search/${query}`

        fetch(url)
            .then((response) => {
                return response.json();
            })
            .then((data) => {
                // console.log(data);

                let text = `<div class='list-group'>`;

                data.forEach((contact) => {
                    text += `<a href='/user/${contact.cId}/contact' class='list-group-item list-group-action'>${contact.name} </a>`
                });
                text += `</div>`;
                $(".search-result").html(text);
                $(".search-result").show();
            })
    }
};

// eye icon in password
function togglePasswordVisibility() {
    var passwordInput = document.getElementById("password");
    var eyeIconSlash = document.getElementById("eye-slash");
    var eyeIcon = document.getElementById("eye");

    if (passwordInput.type === "password") {
        passwordInput.type = "text";
        eyeIconSlash.style.display = "none";
        eyeIcon.style.display = "inline";
    } else {
        passwordInput.type = "password";
        eyeIconSlash.style.display = "inline";
        eyeIcon.style.display = "none";
    }
}

// Payment integration
//first request -> to server to create order

// const paymentStart = () => {
//     console.log("payment....start");
//     var amount=$("#payment_field").val();
//
//     console.log(amount);
//     console.log("payment....start"+amount);
//     if(amount== "" || amount == null){
//         swal("Failed !!","amount is required","error");
//         return;
//     }
//     //we will use ajax TO SEND REQUEST
//     $.ajax({
//         url :'/user/create_order',
//         data:JSON.stringify({amount:amount,info:'order_request'}),
//         contentType:'application/json',
//         type:'POST',
//         dataType:'json',
//         success:function(response){
//
//             console.log(response);
//             if(response.status=="created"){
//                 //open payment form
//                 let options = {
//                     key:"rzp_test_SW3Zn4oCIFMAWU",
//                     amount:response.amount,
//                     currency:"INR",
//                     name:"Smart Contact Manager",
//                     description:"Donation",
//                     image:"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQlult8BayGN-rXj6-eymFL6xhrr3DzvCzrgQ&usqp=CAU",
//                     order_id:response.id,
//
//                     handler:function(response){
//                         console.log(response.razorpay_payment_id)
//                         console.log(response.razorpay_order_id)
//                         console.log(response.razorpay_signature)
//                         console.log();
//
//                         //update payment om server
//                         updatePaymentOnServer(response.razorpay_payment_id,response.razorpay_order_id,"paid");
//                     },
//                     prefill: {
//                         "name": "",
//                         "email": "",
//                         "contact": ""
//                     },
//                     notes: {
//                         "address": "SMC-amritkp"
//                     },
//                     theme: {
//                         "color": "#3399cc"
//                     }
//                 }
//                 let rzp=new Razorpay(options);
//                 rzp.on('payment.failed', function (response){
//                     console.log(response.error.code);
//                     console.log(response.error.description);
//                     console.log(response.error.source);
//                     console.log(response.error.step);
//                     console.log(response.error.reason);
//                     console.log(response.error.metadata.order_id);
//                     console.log(response.error.metadata.payment_id);
//                     swal("Failed !!","Oops payment failed","error");
//                 });
//                 rzp.open();
//             }
//         },
//         error:function(error){
//             console.log(error);
//             alert("something went wrong !!")
//         }
//     })
// }
//
// function updatePaymentOnServer(payment_id,order_id,status){
//     $.ajax({
//         url:"/user/update-order",
//         data:JSON.stringify({
//             payment_id:payment_id,
//             order_id:order_id,
//             status:status,
//         }),
//         contentType:"application/json",
//         type:"POST",
//         dataType:"json",
//         success:function(response){
//             swal("Successfull !!","payment successfull","success");
//         },
//         error:function(error){
//             swal("Failed !!","your payment is successfull,we did not get on server,we will contact you as soon as possible","error");
//         },
//     })};