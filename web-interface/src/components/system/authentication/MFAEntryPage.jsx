import React, {useEffect, useRef, useState} from "react";
import AssetStylesheet from "../../misc/AssetStylesheet";
import AuthenticationService from "../../../services/AuthenticationService";
import moment from "moment";

const authenticationService = new AuthenticationService();

function MFASetupPage(props) {

  const mfaEntryExpiresAt = props.mfaEntryExpiresAt;

  const DEFAULT_TEXT = "Enter Code";

  const [code1, setCode1] = useState("");
  const [code2, setCode2] = useState("");
  const [code3, setCode3] = useState("");
  const [code4, setCode4] = useState("");
  const [code5, setCode5] = useState("");
  const [code6, setCode6] = useState("");

  const [submitText, setSubmitText] = useState(DEFAULT_TEXT)
  const [formSubmitting, setFormSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState(null);
  const [success, setSuccess] = useState(false);

  const refs = {
    code1: useRef(null),
    code2: useRef(null),
    code3: useRef(null),
    code4: useRef(null),
    code5: useRef(null),
    code6: useRef(null),
  }

  useEffect(() => {
    refs.code1.current.focus();
  }, [])

  const onKeyUp = function(e, number) {
    if (e.keyCode === 8 || e.keyCode === 37) {
      // Jump to previous box if there is one.
      if (number > 1) {
        refs["code" + (number - 1)].current.focus();
      }
    } else {
      // Jump to next box if there is one.
      if (number < 6) {
        refs["code" + (number + 1)].current.focus();
      }
    }
  }

  const onChange = function(e, valueSetter) {
    if (e.target.value < 0 || e.target.value > 9) {
      return;
    }

    valueSetter(e.target.value);
  }

  const onSubmit = function() {
    setErrorMessage(null);
    setFormSubmitting(true);
    setSubmitText("Please wait... ");

    const code = code1 + code2 + code3 + code4 + code5 + code6;

    authenticationService.verifyMFA(code, function() {
      setSubmitText("Success! Please wait...");
    }, function() {
      // Artificially wait 1 second so the user sees something happening. (The REST call is extremely fast)
      setTimeout(function(){
        setFormSubmitting(false);
        setSubmitText(DEFAULT_TEXT);
        setSuccess(false);
        setErrorMessage("Invalid code. Please try again.");
        }, 1000);
    });
  }

  const formReady = function() {
    return !formSubmitting && code1 && code2 && code3 && code4 && code5 && code6
  }

  return (
      <React.Fragment>
        <AssetStylesheet filename="onebox.css"/>

        <section className="vh-100 start">
          <div className="container py-5 h-100 mb-5">
            <div className="row d-flex justify-content-center align-items-center h-100">
              <div className="col col-xl-10">
                <div className="card main-card">
                  <div className="row g-0 vh-100">

                    <div className="col-md-5 d-flex align-items-center">
                      <div className="card-body p-4 p-lg-5 text-black">
                        <h1 className="mb-3 pb-3">Multi-Factor Authentication</h1>

                        <p>Enter your multi-factor authentication code to proceed.</p>

                        <hr className="mb-4"/>

                        <form className="row align-items-center totp-validation mb-4">
                          <input type="number" className="form-control" ref={refs.code1} placeholder={"0"}
                                 value={code1}
                                 onChange={(e) => onChange(e, setCode1)}
                                 onKeyUp={(e) => onKeyUp(e, 1)} />
                          <input type="number" className="form-control" ref={refs.code2} placeholder={"0"}
                                 value={code2}
                                 onChange={(e) => onChange(e, setCode2)}
                                 onKeyUp={(e) => onKeyUp(e,2)} />
                          <input type="number" className="form-control" ref={refs.code3} placeholder={"0"}
                                 value={code3}
                                 onChange={(e) => onChange(e, setCode3)}
                                 onKeyUp={(e) => onKeyUp(e,3)} />
                          <input type="number" className="form-control" ref={refs.code4} placeholder={"0"}
                                 value={code4}
                                 onChange={(e) => onChange(e, setCode4)}
                                 onKeyUp={(e) => onKeyUp(e,4)} />
                          <input type="number" className="form-control" ref={refs.code5} placeholder={"0"}
                                 value={code5}
                                 onChange={(e) => onChange(e, setCode5)}
                                 onKeyUp={(e) => onKeyUp(e,5)} />
                          <input type="number" className="form-control" ref={refs.code6} placeholder={"0"}
                                 value={code6}
                                 onChange={(e) => onChange(e, setCode6)}
                                 onKeyUp={(e) => onKeyUp(e,6)} />
                        </form>

                        { errorMessage ? <div className="alert alert-warning mt-2 mb-4">{errorMessage}</div> : null }

                        <button className={"btn " + (success ? "btn-success" : "btn-primary")}
                                onClick={onSubmit}
                                disabled={!formReady()}>
                          {success ? <i className="fa-solid fa-thumbs-up"></i> : null } {submitText}
                        </button>

                        <button className="btn btn-sm btn-link mt-5">Use a recovery code</button>

                        <div className="mt-5">
                          You have <strong title={mfaEntryExpiresAt}>{moment(mfaEntryExpiresAt).fromNow(true)}</strong> remaining
                          to pass the multi-factor challenge. After that, you have to log in again.
                        </div>

                      </div>
                    </div>

                    <div className="col-md-7 d-none d-md-block justify-content-center right-half">
                      <video id="background-video" autoPlay loop muted
                             poster={window.appConfig.assetsUri + "static/loginsplash_preview.jpg"}>
                        <source src={window.appConfig.assetsUri + "static/loginsplash.mp4"} type="video/mp4"/>
                      </video>
                    </div>

                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
      </React.Fragment>
  )

}

export default MFASetupPage;