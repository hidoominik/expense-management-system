import React from 'react'
import { Container, Row, Col } from 'react-bootstrap';
import UnitsService from '../services/UnitsService';
import { useEffect } from 'react';
import UserService from '../services/user.service';
import { useState } from 'react';
import { category } from '../models/category';
import { ListDetail } from '../models/listDetail';
function EditElementListComponent(props) {
    let walletID = '';
    if (sessionStorage && sessionStorage.getItem('walletID')) {
        walletID = JSON.parse(sessionStorage.getItem('walletID'));
    }
    const userData = UserService.getCurrentUser()
    const [units, setUnits] = useState([]);
    const [errorMessage, setErrorMessage]=useState("")
    const [inputName, setInputName] = useState(props.currentName)
    const [inputQuantity, setInputQuantity] = useState(props.currentQuantity)
    const [inputCategoryName, setInputCategoryName] = useState(props.currentCategoryName)
    const [inputCategoryId, setInputCategoryId] = useState(props.currentCategoryId)
    const [listElementData, setListElementData] = useState([])
    useEffect(()=>{
       
        UnitsService.getUnits(userData.token)
        .then((response)=>{
            console.log(response.data)
            setUnits(response.data)
           // setInputCategoryName(Object.values(response.data)[0].name)
            ///setInputCategoryId(Object.values(response.data)[0].id)
            //setInputCategoryName(props.currentCategoryName)
           // setInputCategoryId(props.currentCategoryId)
            
            //setDefaultCategory()
        })
       
        .catch(error=>{
            console.error({error})
        });
               
          
    },[])
   
    function setDefaultCategory(){
        setInputCategoryName(Object.values(units)[0].name)
        setInputCategoryId(Object.values(units)[0].id)
    }

    const handleSubmit = e =>{
        e.preventDefault();
        //setListElementData(new ListDetail(inputName,inputQuantity, inputCategory))
        props.onSubmit({
            id: Math.floor(Math.random() * 100000),
            name: inputName,
            quantity: inputQuantity,
            categoryName: inputCategoryName,
            categoryId: inputCategoryId
           
        })
        console.log(inputName)
        console.log(inputQuantity)
        console.log(inputCategoryName)
        console.log(inputCategoryId)
        setInputCategoryName(Object.values(units)[0].name)
        setInputCategoryId(Object.values(units)[0].id)
        setInputName("")
        setInputQuantity("")
        document.getElementById(Object.values(units)[0].id).checked = true
    }


    const handleChangeName = e =>{
      setInputName(e.target.value)
    }
    const handleChangeQuantity = e =>{
        setInputQuantity(e.target.value)
    }
    function handleChangeCategory (name, id){
        setInputCategoryName(name)
        setInputCategoryId(id)
    }

    return (
        <Container>
            
            <div className="center-content text-label text-size">
                                   {props.title}
                                </div>
                                <div className="separator-line"></div>
             <form name="form"
                        method="post"
                        onSubmit={handleSubmit}
                        >
                        <div className={'form-group'}>
                            <label className="form-label text-size"  htmlFor="Name">Nazwa: </label>
                            <input
                               required
                                type="text"
                                className="form-control"
                                name="name"
                                placeholder={props.currentName}
                                minLength="1"
                                maxLength="45"
                                value = {inputName}
                                onChange={handleChangeName}
                            />
                            
                        </div>

                        <div className={'form-group'}>
                            <label className="form-label text-size" htmlFor="Quantity">Ilość: </label>
                            <input
                                required
                                type="number"
                                step="0.01"
                                className="form-control"
                                name="price"
                                placeholder={props.currentQuantity}
                                maxLength="1000"
                                value={inputQuantity}
                                pattern="^\d{0,8}(\.\d{1,2})?$"
                                onChange={handleChangeQuantity}
                            />
                            
                        </div>
                    <Container className = "box-subcontent">
                      
                    
                        <Row>
                        {
                         
                         units.map(
                            unit =>
                         
                            <Col key = {unit.id} className = "center-content custom-radiobuttons margin-left-text">
                               
                            <label className = "form-label text-size" htmlFor={unit.id}>
                              <input type="radio" id={unit.id} name="unit" value={unit.name} 
                                   defaultChecked = {unit.name === props.currentCategoryName}
                                  
                                  onChange={(e)=>handleChangeCategory( unit.name, unit.id)}
                                  >
                                      
                              </input>
                              
                              <div className="checkmark icons-size-2"></div>
                               {unit.name}</label>
                              
                          </Col> 
                         
                         )}
                   </Row> 
                </Container>    
               
                <div className="error-text text-size">
                    {errorMessage}
                </div>
                <br />
                        <button
                            className="btn btn-primary btn-block form-button text-size"
                            id = "mainbuttonstyle"
                            type = "submit"
                            onClick={e =>  {
                                //window.location.href='/wallet' 
                            }}
                            >
                            Zapisz zmiany
                        </button>
                        <button
                            className="btn btn-primary btn-block form-button text-size"
                            id = "mainbuttonstyle"
                           
                            onClick={e =>  {
                                props.cancel()
                            }}
                            >
                            Anuluj
                        </button>
                </form>
                                
                       
        </Container>
    )
}

export default EditElementListComponent
