const Button = ({ label, type = 'button', onClick, disabled = false }) => {
  return (
    <button type={type} className="action-button" onClick={onClick} disabled={disabled}>
      {label}
    </button>
  );
};

export default Button;
