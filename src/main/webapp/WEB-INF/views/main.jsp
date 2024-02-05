<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <link rel="stylesheet" href="resources/static/style.css">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>VetVision-AI</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-T3c6CoIi6uLrA9TneNEoa7RxnatzjcDSCmG1MXxSR1GAsXEV/Dwwykc2MPK8M2HN" crossorigin="anonymous">
</head>
<body>
  <div class="upload-container" id="upload-container">
  
    <h2>AI pet health care</h2>
    <p class="lead">이미지 기반 반려동물 건강 분석</p>
    
    <!-- Upload  -->
    <form id="file-upload-form" class="uploader">
      <input id="file-upload" type="file" name="fileUpload" accept="image/*" />
    
      <label for="file-upload" id="file-drag" class="file-upload">
        <img id="file-image" src="#" alt="Preview" class="hidden">
        <div id="start">
          <i class="fa fa-download" aria-hidden="true"></i>
          <div>Select a file or drag here</div>
          <div id="notimage" class="hidden">Please select an image</div>
          <span id="file-upload-btn" class="btn btn-primary">Select a file</span>
        </div>
        <div id="response" class="hidden">
          <div id="messages"></div>
          <progress class="progress" id="file-progress" value="0">
            <span>0</span>%
          </progress>
        </div>
      </label>
      
    </form>

  </div>
  <div class="text-container" id="container">
    <input type="text" id="resizer" class="input-box" placeholder="예: 강아지의 피부가 붉어(필수X, 사진만O)">
    <div>
      <button class="bubbly-button" id="analyse-button">분석시작</button>
    </div>
    <div class="mb-3">
      <label for="exampleFormControlTextarea1" class="form-label">분석 결과</label>
      <textarea class="form-control" aria-label="readonly input example" rows="10" readonly></textarea>
    </div>
  </div>
  
  <!-- 로더 -->
  <div class="overlay" style="display: none;">
    <div class="loader"></div>
  </div>
  

</body>
</html>

<script>

// 분석시작 버튼 이벤트
document.getElementById('analyse-button').addEventListener('click', function() {
    // 데이터 수집
    var imageUrl = document.getElementById('file-image').src;
    var inputText = document.getElementById('resizer').value;

    imageUrl = imageUrl.includes('base64') ? imageUrl : null;

    if(imageUrl === null && inputText === '') {
        alert('이미지를 업로드하거나 텍스트를 입력해주세요.');
        return;
    }
    // 오버레이와 로더 표시
    document.querySelector('.overlay').style.display = 'flex'; // flex로 설정하여 로더를 중앙에 위치시킵니다.
     

    // 서버로 데이터 전송
    fetch('/vet', { 
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            imageUrl: imageUrl,
            inputText: inputText
        })
    })
    .then(response => response.text())
    .then(text => {
        // 오버레이와 로더 숨기기
        document.querySelector('.overlay').style.display = 'none';
        document.querySelector('.form-control').value = text;
    })
    .catch((error) => {
        // 오류 발생 시에도 오버레이와 로더 숨기기
        document.querySelector('.overlay').style.display = 'none';
        console.error('Error:', error); 
    });
});


// File Upload
function ekUpload(){
  function Init() {

    console.log("Upload Initialised");
    
    var fileSelect    = document.getElementById('file-upload'),
        fileDrag      = document.getElementById('file-drag'),
        submitButton  = document.getElementById('submit-button');

    fileSelect.addEventListener('change', fileSelectHandler, false);

    // Is XHR2 available?
    var xhr = new XMLHttpRequest();
    if (xhr.upload) {
      // File Drop
      fileDrag.addEventListener('dragover', fileDragHover, false);
      fileDrag.addEventListener('dragleave', fileDragHover, false);
      fileDrag.addEventListener('drop', fileSelectHandler, false);
    }
  }
  

  function fileDragHover(e) {
    var fileDrag = document.getElementById('file-drag');

    e.stopPropagation();
    e.preventDefault();

    fileDrag.className = (e.type === 'dragover' ? 'hover' : 'modal-body file-upload');
  }

  function fileSelectHandler(e) {
    // Fetch FileList object
    var files = e.target.files || e.dataTransfer.files;

    // Cancel event and hover styling
    fileDragHover(e);

    // Process all File objects
    for (var i = 0, f; f = files[i]; i++) {
      parseFile(f);
      uploadFile(f);
    }
  }

  // Output
  function output(msg) {
    // Response
    var m = document.getElementById('messages');
    m.innerHTML = msg;
  }

  function parseFile(file) {

    console.log(file.name);
    output(
      '<strong>' + encodeURI(file.name) + '</strong>'
    );
    
    // var fileType = file.type;
    // console.log(fileType);
    var imageName = file.name;

    var isGood = (/\.(?=gif|jpg|png|jpeg)/gi).test(imageName);
    if (isGood) {
      document.getElementById('start').classList.add("hidden");
      document.getElementById('response').classList.remove("hidden");
      document.getElementById('notimage').classList.add("hidden");
        // Base64 인코딩을 사용한 Thumbnail Preview
        var reader = new FileReader();
        reader.onloadend = function() {
            document.getElementById('file-image').classList.remove("hidden");
            document.getElementById('file-image').src = reader.result;
        }
        reader.readAsDataURL(file);
    }
    else {
      document.getElementById('file-image').classList.add("hidden");
      document.getElementById('notimage').classList.remove("hidden");
      document.getElementById('start').classList.remove("hidden");
      document.getElementById('response').classList.add("hidden");
      document.getElementById("file-upload-form").reset();
    }
  }

  function setProgressMaxValue(e) {
    var pBar = document.getElementById('file-progress');

    if (e.lengthComputable) {
      pBar.max = e.total;
    }
  }

  function updateFileProgress(e) {
    var pBar = document.getElementById('file-progress');

    if (e.lengthComputable) {
      pBar.value = e.loaded;
    }
  }

  function uploadFile(file) {

    var xhr = new XMLHttpRequest(),
      fileInput = document.getElementById('class-roster-file'),
      pBar = document.getElementById('file-progress'),
      fileSizeLimit = 1024; // In MB
    if (xhr.upload) {
      // Check if file is less than x MB
      if (file.size <= fileSizeLimit * 1024 * 1024) {
        // Progress bar
        pBar.style.display = 'inline';
        xhr.upload.addEventListener('loadstart', setProgressMaxValue, false);
        xhr.upload.addEventListener('progress', updateFileProgress, false);

        // File received / failed
        xhr.onreadystatechange = function(e) {
          if (xhr.readyState == 4) {
            // Everything is good!

            // progress.className = (xhr.status == 200 ? "success" : "failure");
            // document.location.reload(true);
          }
        };

        // Start upload
        xhr.open('POST', document.getElementById('file-upload-form').action, true);
        xhr.setRequestHeader('X-File-Name', file.name);
        xhr.setRequestHeader('X-File-Size', file.size);
        xhr.setRequestHeader('Content-Type', 'multipart/form-data');
        xhr.send(file);
      } else {
        output('Please upload a smaller file (< ' + fileSizeLimit + ' MB).');
      }
    }
  }

  // Check for the various File API support.
  if (window.File && window.FileList && window.FileReader) {
    Init();
  } else {
    document.getElementById('file-drag').style.display = 'none';
  }
}
ekUpload();



function Resizer( element ) {

var inputBox = element;
var cssRules = window.getComputedStyle(inputBox);
var maxFontSize = parseInt(cssRules.getPropertyValue("font-size"));
var minFontSize = 11; // 11 is pretty damn small!
var currentFontSize = maxFontSize;
var maxScrollWidth = parseInt(cssRules.getPropertyValue("width"))
var fontFamily = cssRules.getPropertyValue("font-family");
var currentText = inputBox.value;

// Canvas used to check text widths.
var canvas = document.createElement('canvas');
var context = canvas.getContext('2d');

var initialize = function() {

    inputBox.oninput = onUpdate;
}

var onUpdate = function(event) {

    var width;
    // Some text has been deleted!
    if (inputBox.value.length < currentText.length) {
        width = checkTextWidth(inputBox.value, currentFontSize + 1);

        while (width < maxScrollWidth && currentFontSize < maxFontSize) {
            currentFontSize += 1;
            inputBox.style.fontSize = currentFontSize + 'px';
            width = checkTextWidth(inputBox.value, currentFontSize + 1);
        }

        currentText = inputBox.value;
        return;

    }

    var width = checkTextWidth(inputBox.value, currentFontSize);

    // Shrink
    while (currentFontSize > minFontSize && width > maxScrollWidth) {
        currentFontSize -= 1;
        inputBox.style.fontSize = currentFontSize + 'px';
        width = checkTextWidth(inputBox.value, currentFontSize);
    }

    currentText = inputBox.value;
}

var checkTextWidth = function(text, size) {
    context.font = size + "px " + fontFamily;

    if (context.fillText) {
        return context.measureText(text).width;
    } else if (context.mozDrawText) {
        return context.mozMeasureText(text);
    }
}

// Initialize the auto adapt functionality.
initialize();
}

Resizer( document.getElementById( 'resizer' ) );

// ******** 버블 버튼 js ********
var animateButton = function(e) {

e.preventDefault;
//reset animation
e.target.classList.remove('animate');

e.target.classList.add('animate');
setTimeout(function(){
  e.target.classList.remove('animate');
},700);
};

var bubblyButtons = document.getElementsByClassName("bubbly-button");

for (var i = 0; i < bubblyButtons.length; i++) {
bubblyButtons[i].addEventListener('click', animateButton, false);
}

</script>