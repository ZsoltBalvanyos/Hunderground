var focus;

function setFocus(id) {
    focus = document.getElementById(id).value;
}

function sendMemo(id) {
    var memo = document.getElementById(id).value;
    if(memo != focus) {
        var req = new XMLHttpRequest();
        req.open("PUT", "calendar/?id=" + id + "&memo=" + memo) ;
        req.send();
    }
}


function editSong(songId, artist, title, key, target) {
    $("#artist").val(artist);
    $("#title").val(title);
    $("#key").val(key);

    openModalForUpdate(songId, target)
}

function updateSong(songId, target) {
    var req = new XMLHttpRequest();
    req.open("POST", "song/" + $("#artist").val() + "/" + $("#title").val() + "/" + $("#key").val() + "/" + target);

    req.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200) {
            deleteSong(songId);
        }
    };

    req.send();
}

function addSong(target) {
    var req = new XMLHttpRequest();
    req.open("POST", "song/" + $("#artist").val() + "/" + $("#title").val() + "/" + $("#key").val() + "/" + target);

    req.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200) {
            location.reload();
        }
    };

    req.send();
}

function deleteSong(songId) {
    var req = new XMLHttpRequest();
    req.open("DELETE", "song/" + songId);

    req.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200) {
            location.reload();
        }
    };

    req.send();
}

function moveUp(songId, artist, title, key) {
    var reqDelete = new XMLHttpRequest();
    var reqPost = new XMLHttpRequest();
    reqDelete.open("DELETE", "song/" + songId);
    reqPost.open("POST", "song/" + artist + "/" + title + "/" + key + "/ready");

    reqDelete.send();
    reqPost.send();

    reqPost.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200) {
            location.reload();
        }
    };
}

function openModal(target) {
    document.getElementById("modal-title").innerText = "Add new song";
    $("#artist").val("");
    $("#title").val("");
    document.getElementById("submitModal").onclick = function(){addSong(target);}

    $('#newSong').modal('show');
}

function openModalForUpdate(songId, target) {
    document.getElementById("modal-title").innerText = "Update song";
    document.getElementById("submitModal").onclick = function(){updateSong(songId, target);}

    $('#newSong').modal('show');
}

function addRehearsal(date) {
    focus = date
    $('#newRehearsal').modal('show')
}

function addGig(date) {
    focus = date
    $('#newGig').modal('show')
}

function addMemo(date) {
    focus = date
    $('#newMemo').modal('show')
}

function addHoliday(date) {
    focus = date
    $('#holidayTill').attr('min', date)
    $('#newHoliday').modal('show')
}

function saveEvent(type) {
    var req = new XMLHttpRequest();

    if(type === 'gig') {
        req.open("POST", "event/gig/" + focus + "/" + $("#gigLocation").val());
    } else if(type === 'rehearsal') {
        req.open("POST", "event/rehearsal/" + focus + "/" + $("#rehearsalLocation").val()+ "/" + $("#rehearsalStart").val()+ "/" + $("#rehearsalDuration").val());
    } else if(type === 'memo') {
        req.open("POST", "event/memo/" + focus + "/" + $("#memo").val());
    } else if(type === 'holiday') {
        req.open("POST", "event/holiday/" + focus + "/" + $("#person").val()+ "/" + $("#holidayTill").val());
    }

    req.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200) {
            location.reload();
        }
    };

    req.send();
}

function editRehearsal(eventId, date, loc) {
    focus = date

    $("#rehearsalLocationEdit").val(loc);

    $("#deleteRehearsal").click( function() {
        var req = new XMLHttpRequest();
        req.open("DELETE", "event/rehearsal/" + eventId);
        $('#editRehearsal').modal('hide')

        req.onreadystatechange = function() {
            if (this.readyState == 4 && this.status == 200) {
                location.reload();
            }
        };
        req.send();
    });

    $("#submitRehearsal").click( function() {
        var req = new XMLHttpRequest();
        req.open("PUT", "event/rehearsal/" + focus + "/" + $("#rehearsalLocationEdit").val() + "/" + eventId);
        $('#editRehearsal').modal('hide')

        req.onreadystatechange = function() {
            if (this.readyState == 4 && this.status == 200) {
                location.reload();
            }
        };
        req.send();
    });

    $('#editRehearsal').modal('show')
}

function editGig(eventId, date, loc) {
    focus = date

    $("#gigLocationEdit").val(loc);

    $("#deleteGig").click(function(){
        var req = new XMLHttpRequest();
        req.open("DELETE", "event/gig/" + eventId);
        $('#editGig').modal('hide')

        req.onreadystatechange = function() {
            if (this.readyState == 4 && this.status == 200) {
                location.reload();
            }
        };
        req.send();
    });

    $("#submitGig").click( function() {
        var req = new XMLHttpRequest();
        req.open("PUT", "event/gig/" + focus + "/" + $("#gigLocationEdit").val() + "/" + eventId);
        $('#editGig').modal('hide')

        req.onreadystatechange = function() {
            if (this.readyState == 4 && this.status == 200) {
                location.reload();
            }
        };
        req.send();
    });

    $('#editGig').modal('show')
}

function editMemo(eventId, date, memo) {
    focus = date

    $("#memoEdit").val(memo);

    $("#deleteMemo").click(function(){
        var req = new XMLHttpRequest();
        req.open("DELETE", "event/memo/" + eventId);
        $('#editMemo').modal('hide')

        req.onreadystatechange = function() {
            if (this.readyState == 4 && this.status == 200) {
                location.reload();
            }
        };
        req.send();
    });

    $("#submitMemo").click( function() {
        var req = new XMLHttpRequest();
        req.open("PUT", "event/memo/" + focus + "/" + $("#memoEdit").val() + "/" + eventId);
        $('#editMemo').modal('hide')

        req.onreadystatechange = function() {
            if (this.readyState == 4 && this.status == 200) {
                location.reload();
            }
        };
        req.send();
    });

    $('#editMemo').modal('show')
}

function editHoliday(eventId, date, userId, start, finish) {
    focus = date
    $('#holidayTillEdit').attr('min', start);
    $('#holidayStartEdit').val(start);
    $('#holidayTillEdit').val(finish);
    $("#personEdit").val(userId);

    $("#deleteHoliday").click(function(){
        var req = new XMLHttpRequest();
        req.open("DELETE", "event/holiday/" + userId + "/" + start + "/" + finish);
        $('#editHoliday').modal('hide')

        req.onreadystatechange = function() {
            if (this.readyState == 4 && this.status == 200) {
                location.reload();
            }
        }
        req.send();
    });

    $("#submitHoliday").click( function() {
        var req = new XMLHttpRequest();
        req.open("PUT", "event/holiday/" + $("#personEdit").val() + "/" + start + "/" + finish + "/" + $("#holidayStartEdit").val()  + "/" + $("#holidayTillEdit").val());
        $('#editHoliday').modal('hide')

        req.onreadystatechange = function() {
            if (this.readyState == 4 && this.status == 200) {
                location.reload();
            }
        };
        req.send();
    });

    $('#editHoliday').modal('show')
}
