import { ReactNode } from 'react';
import styles from './Table.module.css';

interface Column {
  key: string;
  title: string;
  width?: string;
  align?: 'left' | 'center' | 'right';
  sortable?: boolean;
  render?: (value: any, record: any, index: number) => ReactNode;
}

interface TableProps {
  columns: Column[];
  data: any[];
  loading?: boolean;
  emptyText?: string;
  hoverable?: boolean;
  striped?: boolean;
  compact?: boolean;
  onRowClick?: (record: any, index: number) => void;
  className?: string;
  rowClassName?: (record: any, index: number) => string;
}

interface TableHeaderProps {
  children: ReactNode;
  className?: string;
}

interface TableBodyProps {
  children: ReactNode;
  className?: string;
}

interface TableRowProps {
  children: ReactNode;
  onClick?: () => void;
  className?: string;
  hoverable?: boolean;
}

interface TableCellProps {
  children: ReactNode;
  align?: 'left' | 'center' | 'right';
  width?: string;
  className?: string;
}

export function TableHeader({ children, className = '' }: TableHeaderProps) {
  return (
    <thead className={`${styles.header} ${className}`}>
      {children}
    </thead>
  );
}

export function TableBody({ children, className = '' }: TableBodyProps) {
  return (
    <tbody className={`${styles.body} ${className}`}>
      {children}
    </tbody>
  );
}

export function TableRow({ children, onClick, className = '', hoverable = false }: TableRowProps) {
  const rowClasses = [
    styles.row,
    hoverable && styles.hoverable,
    onClick && styles.clickable,
    className
  ].filter(Boolean).join(' ');

  return (
    <tr className={rowClasses} onClick={onClick}>
      {children}
    </tr>
  );
}

export function TableCell({ children, align = 'left', width, className = '' }: TableCellProps) {
  const cellClasses = [
    styles.cell,
    styles[`align-${align}`],
    className
  ].filter(Boolean).join(' ');

  return (
    <td className={cellClasses} style={{ width }}>
      {children}
    </td>
  );
}

export function TableHeaderCell({ children, align = 'left', width, className = '' }: TableCellProps) {
  const cellClasses = [
    styles.headerCell,
    styles[`align-${align}`],
    className
  ].filter(Boolean).join(' ');

  return (
    <th className={cellClasses} style={{ width }}>
      {children}
    </th>
  );
}

export default function Table({
  columns,
  data,
  loading = false,
  emptyText = '데이터가 없습니다',
  hoverable = true,
  striped = false,
  compact = false,
  onRowClick,
  className = '',
  rowClassName
}: TableProps) {
  const tableClasses = [
    styles.table,
    striped && styles.striped,
    compact && styles.compact,
    className
  ].filter(Boolean).join(' ');

  const renderCell = (column: Column, record: any, index: number) => {
    const value = record[column.key];
    
    if (column.render) {
      return column.render(value, record, index);
    }
    
    return value;
  };

  if (loading) {
    return (
      <div className={styles.container}>
        <div className={styles.loading}>
          <div className={styles.spinner}></div>
          <span>로딩 중...</span>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <table className={tableClasses}>
        <TableHeader>
          <TableRow>
            {columns.map((column) => (
              <TableHeaderCell
                key={column.key}
                align={column.align}
                width={column.width}
              >
                {column.title}
                {column.sortable && (
                  <span className={styles.sortIcon}>
                    <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
                      <path d="M6 2L8 4H4L6 2Z" fill="#9CA3AF"/>
                      <path d="M6 10L4 8H8L6 10Z" fill="#9CA3AF"/>
                    </svg>
                  </span>
                )}
              </TableHeaderCell>
            ))}
          </TableRow>
        </TableHeader>
        
        <TableBody>
          {data.length === 0 ? (
            <TableRow>
              <TableCell className={styles.emptyCell}>
                <div className={styles.empty}>
                  {emptyText}
                </div>
              </TableCell>
            </TableRow>
          ) : (
            data.map((record, index) => (
              <TableRow
                key={index}
                hoverable={hoverable}
                onClick={onRowClick ? () => onRowClick(record, index) : undefined}
                className={rowClassName ? rowClassName(record, index) : ''}
              >
                {columns.map((column) => (
                  <TableCell
                    key={column.key}
                    align={column.align}
                    width={column.width}
                  >
                    {renderCell(column, record, index)}
                  </TableCell>
                ))}
              </TableRow>
            ))
          )}
        </TableBody>
      </table>
    </div>
  );
}